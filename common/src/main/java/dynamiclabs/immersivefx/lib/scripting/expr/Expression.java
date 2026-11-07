/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.lib.scripting.expr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class Expression {

    private final String source;
    private final Node root;

    private Expression(@Nonnull final String source, @Nonnull final Node root) {
        this.source = source;
        this.root = root;
    }

    @Nonnull
    public String getSource() {
        return this.source;
    }

    @Nullable
    public Object evaluate() {
        return JsRuntime.toJava(this.root.eval());
    }

    @Nonnull
    public static Expression compile(@Nonnull final String source, @Nonnull final Bindings bindings) {
        final Parser parser = new Parser(source, bindings);
        return new Expression(source, parser.parseAll());
    }

    @Override
    public String toString() {
        return this.source;
    }

    private interface Node {
        Object eval();
    }

    private record Constant(Object value) implements Node {
        @Override
        public Object eval() {
            return this.value;
        }
    }

    private record Variable(Bindings.Slot slot) implements Node {
        @Override
        public Object eval() {
            final Object v = this.slot.value;
            if (v == Undefined.INSTANCE)
                throw new ExpressionException("ReferenceError: \"" + this.slot.getName() + "\" is not defined");
            return v;
        }
    }

    private record Member(Node target, String name) implements Node {
        @Override
        public Object eval() {
            return JsRuntime.getProperty(this.target.eval(), this.name);
        }
    }

    private record Index(Node target, Node key) implements Node {
        @Override
        public Object eval() {
            final Object t = this.target.eval();
            return JsRuntime.getIndexed(t, this.key.eval());
        }
    }

    private record MethodCall(Node target, String name, Node[] args) implements Node {
        @Override
        public Object eval() {
            final Object t = this.target.eval();
            return JsRuntime.invoke(t, this.name, evalArgs(this.args));
        }
    }

    private record FunctionCall(Node function, Node[] args) implements Node {
        @Override
        public Object eval() {
            return JsRuntime.call(this.function.eval(), evalArgs(this.args));
        }
    }

    private static Object[] evalArgs(@Nonnull final Node[] args) {
        final Object[] values = new Object[args.length];
        for (int i = 0; i < args.length; i++)
            values[i] = args[i].eval();
        return values;
    }

    private record Not(Node operand) implements Node {
        @Override
        public Object eval() {
            return !JsRuntime.toBoolean(this.operand.eval());
        }
    }

    private record Negate(Node operand) implements Node {
        @Override
        public Object eval() {
            return -JsRuntime.toNumber(this.operand.eval());
        }
    }

    private record ToNumber(Node operand) implements Node {
        @Override
        public Object eval() {
            return JsRuntime.toNumber(this.operand.eval());
        }
    }

    private record And(Node left, Node right) implements Node {
        @Override
        public Object eval() {
            final Object l = this.left.eval();
            return JsRuntime.toBoolean(l) ? this.right.eval() : l;
        }
    }

    private record Or(Node left, Node right) implements Node {
        @Override
        public Object eval() {
            final Object l = this.left.eval();
            return JsRuntime.toBoolean(l) ? l : this.right.eval();
        }
    }

    private record Conditional(Node test, Node whenTrue, Node whenFalse) implements Node {
        @Override
        public Object eval() {
            return JsRuntime.toBoolean(this.test.eval()) ? this.whenTrue.eval() : this.whenFalse.eval();
        }
    }

    private enum BinaryOp {
        EQ, NE, SEQ, SNE, LT, LE, GT, GE, ADD, SUB, MUL, DIV, MOD
    }

    private record Binary(BinaryOp op, Node left, Node right) implements Node {
        @Override
        public Object eval() {
            final Object l = this.left.eval();
            final Object r = this.right.eval();
            switch (this.op) {
                case EQ:
                    return JsRuntime.looseEquals(l, r);
                case NE:
                    return !JsRuntime.looseEquals(l, r);
                case SEQ:
                    return JsRuntime.strictEquals(l, r);
                case SNE:
                    return !JsRuntime.strictEquals(l, r);
                case LT: {
                    final int c = JsRuntime.compare(l, r);
                    return c != -2 && c < 0;
                }
                case LE: {
                    final int c = JsRuntime.compare(l, r);
                    return c != -2 && c <= 0;
                }
                case GT: {
                    final int c = JsRuntime.compare(l, r);
                    return c != -2 && c > 0;
                }
                case GE: {
                    final int c = JsRuntime.compare(l, r);
                    return c != -2 && c >= 0;
                }
                case ADD:
                    return JsRuntime.add(l, r);
                case SUB:
                    return JsRuntime.toNumber(l) - JsRuntime.toNumber(r);
                case MUL:
                    return JsRuntime.toNumber(l) * JsRuntime.toNumber(r);
                case DIV:
                    return JsRuntime.toNumber(l) / JsRuntime.toNumber(r);
                case MOD:
                    return JsRuntime.toNumber(l) % JsRuntime.toNumber(r);
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private enum TokenType {
        NUMBER, STRING, IDENT, OP, EOF
    }

    private record Token(TokenType type, String text, Object value, int pos) {
    }

    private static final class Parser {
        private final String source;
        private final Bindings bindings;
        private final List<Token> tokens;
        private int index;

        Parser(@Nonnull final String source, @Nonnull final Bindings bindings) {
            this.source = source;
            this.bindings = bindings;
            this.tokens = tokenize(source);
        }

        Node parseAll() {
            if (peek().type == TokenType.EOF)
                throw error("Empty expression", peek());
            final Node node = parseConditional();
            if (peek().type != TokenType.EOF)
                throw error("Unexpected '" + peek().text + "'", peek());
            return node;
        }

        private Node parseConditional() {
            final Node test = parseOr();
            if (acceptOp("?")) {
                final Node whenTrue = parseConditional();
                expectOp(":");
                final Node whenFalse = parseConditional();
                return new Conditional(test, whenTrue, whenFalse);
            }
            return test;
        }

        private Node parseOr() {
            Node left = parseAnd();
            while (acceptOp("||"))
                left = new Or(left, parseAnd());
            return left;
        }

        private Node parseAnd() {
            Node left = parseEquality();
            while (acceptOp("&&"))
                left = new And(left, parseEquality());
            return left;
        }

        private Node parseEquality() {
            Node left = parseRelational();
            while (true) {
                if (acceptOp("=="))
                    left = new Binary(BinaryOp.EQ, left, parseRelational());
                else if (acceptOp("!="))
                    left = new Binary(BinaryOp.NE, left, parseRelational());
                else if (acceptOp("==="))
                    left = new Binary(BinaryOp.SEQ, left, parseRelational());
                else if (acceptOp("!=="))
                    left = new Binary(BinaryOp.SNE, left, parseRelational());
                else
                    return left;
            }
        }

        private Node parseRelational() {
            Node left = parseAdditive();
            while (true) {
                if (acceptOp("<"))
                    left = new Binary(BinaryOp.LT, left, parseAdditive());
                else if (acceptOp("<="))
                    left = new Binary(BinaryOp.LE, left, parseAdditive());
                else if (acceptOp(">"))
                    left = new Binary(BinaryOp.GT, left, parseAdditive());
                else if (acceptOp(">="))
                    left = new Binary(BinaryOp.GE, left, parseAdditive());
                else
                    return left;
            }
        }

        private Node parseAdditive() {
            Node left = parseMultiplicative();
            while (true) {
                if (acceptOp("+"))
                    left = new Binary(BinaryOp.ADD, left, parseMultiplicative());
                else if (acceptOp("-"))
                    left = new Binary(BinaryOp.SUB, left, parseMultiplicative());
                else
                    return left;
            }
        }

        private Node parseMultiplicative() {
            Node left = parseUnary();
            while (true) {
                if (acceptOp("*"))
                    left = new Binary(BinaryOp.MUL, left, parseUnary());
                else if (acceptOp("/"))
                    left = new Binary(BinaryOp.DIV, left, parseUnary());
                else if (acceptOp("%"))
                    left = new Binary(BinaryOp.MOD, left, parseUnary());
                else
                    return left;
            }
        }

        private Node parseUnary() {
            if (acceptOp("!"))
                return new Not(parseUnary());
            if (acceptOp("-")) {
                final Node operand = parseUnary();
                if (operand instanceof Constant c && c.value() instanceof Double d)
                    return new Constant(-d);
                return new Negate(operand);
            }
            if (acceptOp("+"))
                return new ToNumber(parseUnary());
            return parsePostfix();
        }

        private Node parsePostfix() {
            Node node = parsePrimary();
            while (true) {
                if (acceptOp(".")) {
                    final Token name = next();
                    if (name.type != TokenType.IDENT)
                        throw error("Property name expected after '.'", name);
                    if (acceptOp("("))
                        node = new MethodCall(node, name.text, parseArguments());
                    else
                        node = new Member(node, name.text);
                } else if (acceptOp("[")) {
                    final Node key = parseConditional();
                    expectOp("]");
                    node = new Index(node, key);
                } else if (acceptOp("(")) {
                    node = new FunctionCall(node, parseArguments());
                } else {
                    return node;
                }
            }
        }

        private Node[] parseArguments() {
            final List<Node> args = new ArrayList<>();
            if (!acceptOp(")")) {
                do {
                    args.add(parseConditional());
                } while (acceptOp(","));
                expectOp(")");
            }
            return args.toArray(new Node[0]);
        }

        private Node parsePrimary() {
            final Token t = next();
            switch (t.type) {
                case NUMBER:
                case STRING:
                    return new Constant(t.value);
                case IDENT:
                    switch (t.text) {
                        case "true":
                        case "TRUE":
                            return new Constant(Boolean.TRUE);
                        case "false":
                        case "FALSE":
                            return new Constant(Boolean.FALSE);
                        case "null":
                            return new Constant(null);
                        case "undefined":
                            return new Constant(Undefined.INSTANCE);
                        case "NaN":
                            return new Constant(Double.NaN);
                        case "Infinity":
                            return new Constant(Double.POSITIVE_INFINITY);
                        default:
                            return new Variable(this.bindings.slot(t.text));
                    }
                case OP:
                    if ("(".equals(t.text)) {
                        final Node inner = parseConditional();
                        expectOp(")");
                        return inner;
                    }
                    throw error("Unexpected '" + t.text + "'", t);
                default:
                    throw error("Unexpected end of expression", t);
            }
        }

        private Token peek() {
            return this.tokens.get(this.index);
        }

        private Token next() {
            final Token t = this.tokens.get(this.index);
            if (t.type != TokenType.EOF)
                this.index++;
            return t;
        }

        private boolean acceptOp(@Nonnull final String op) {
            final Token t = peek();
            if (t.type == TokenType.OP && op.equals(t.text)) {
                this.index++;
                return true;
            }
            return false;
        }

        private void expectOp(@Nonnull final String op) {
            if (!acceptOp(op))
                throw error("'" + op + "' expected", peek());
        }

        private ExpressionException error(@Nonnull final String msg, @Nonnull final Token at) {
            return new ExpressionException("SyntaxError: " + msg + " at position " + at.pos + " in: " + this.source);
        }

        private static final String[] OPERATORS = {
                "===", "!==", "==", "!=", "<=", ">=", "&&", "||",
                "<", ">", "+", "-", "*", "/", "%", "!", "(", ")", "[", "]", ".", ",", "?", ":"
        };

        private static List<Token> tokenize(@Nonnull final String s) {
            final List<Token> result = new ArrayList<>();
            final int len = s.length();
            int i = 0;
            while (i < len) {
                final char c = s.charAt(i);
                if (Character.isWhitespace(c)) {
                    i++;
                    continue;
                }
                if (c == ';') {
                    int j = i;
                    while (j < len && (s.charAt(j) == ';' || Character.isWhitespace(s.charAt(j))))
                        j++;
                    if (j < len)
                        throw new ExpressionException("SyntaxError: only a single expression is supported, found ';' at position " + i + " in: " + s);
                    break;
                }
                if (Character.isDigit(c) || (c == '.' && i + 1 < len && Character.isDigit(s.charAt(i + 1)))) {
                    i = readNumber(s, i, result);
                    continue;
                }
                if (c == '\'' || c == '"') {
                    i = readString(s, i, result);
                    continue;
                }
                if (Character.isJavaIdentifierStart(c)) {
                    int j = i + 1;
                    while (j < len && Character.isJavaIdentifierPart(s.charAt(j)))
                        j++;
                    final String ident = s.substring(i, j);
                    result.add(new Token(TokenType.IDENT, ident, ident, i));
                    i = j;
                    continue;
                }
                boolean matched = false;
                for (final String op : OPERATORS) {
                    if (s.startsWith(op, i)) {
                        result.add(new Token(TokenType.OP, op, op, i));
                        i += op.length();
                        matched = true;
                        break;
                    }
                }
                if (!matched)
                    throw new ExpressionException("SyntaxError: Unexpected character '" + c + "' at position " + i + " in: " + s);
            }
            result.add(new Token(TokenType.EOF, "<end>", null, len));
            return result;
        }

        private static int readNumber(@Nonnull final String s, final int start, @Nonnull final List<Token> out) {
            final int len = s.length();
            int i = start;
            if (s.charAt(i) == '0' && i + 1 < len && (s.charAt(i + 1) == 'x' || s.charAt(i + 1) == 'X')) {
                i += 2;
                final int digits = i;
                while (i < len && Character.digit(s.charAt(i), 16) >= 0)
                    i++;
                if (i == digits)
                    throw new ExpressionException("SyntaxError: Invalid hex number at position " + start + " in: " + s);
                out.add(new Token(TokenType.NUMBER, s.substring(start, i), (double) Long.parseLong(s.substring(digits, i), 16), start));
                return i;
            }
            while (i < len && Character.isDigit(s.charAt(i)))
                i++;
            if (i < len && s.charAt(i) == '.') {
                i++;
                while (i < len && Character.isDigit(s.charAt(i)))
                    i++;
            }
            if (i < len && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
                int j = i + 1;
                if (j < len && (s.charAt(j) == '+' || s.charAt(j) == '-'))
                    j++;
                if (j < len && Character.isDigit(s.charAt(j))) {
                    while (j < len && Character.isDigit(s.charAt(j)))
                        j++;
                    i = j;
                }
            }
            if (i < len && Character.isJavaIdentifierStart(s.charAt(i)))
                throw new ExpressionException("SyntaxError: Invalid number at position " + start + " in: " + s);
            final String text = s.substring(start, i);
            out.add(new Token(TokenType.NUMBER, text, Double.parseDouble(text), start));
            return i;
        }

        private static int readString(@Nonnull final String s, final int start, @Nonnull final List<Token> out) {
            final char quote = s.charAt(start);
            final StringBuilder builder = new StringBuilder();
            final int len = s.length();
            int i = start + 1;
            while (true) {
                if (i >= len)
                    throw new ExpressionException("SyntaxError: Unterminated string at position " + start + " in: " + s);
                final char c = s.charAt(i++);
                if (c == quote)
                    break;
                if (c == '\\') {
                    if (i >= len)
                        throw new ExpressionException("SyntaxError: Unterminated string at position " + start + " in: " + s);
                    final char e = s.charAt(i++);
                    switch (e) {
                        case 'n' -> builder.append('\n');
                        case 't' -> builder.append('\t');
                        case 'r' -> builder.append('\r');
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case '0' -> builder.append('\0');
                        case 'u' -> {
                            if (i + 4 > len)
                                throw new ExpressionException("SyntaxError: Invalid unicode escape at position " + (i - 2) + " in: " + s);
                            builder.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                            i += 4;
                        }
                        default -> builder.append(e);
                    }
                } else {
                    builder.append(c);
                }
            }
            final String value = builder.toString();
            out.add(new Token(TokenType.STRING, s.substring(start, i), value, start));
            return i;
        }
    }
}
