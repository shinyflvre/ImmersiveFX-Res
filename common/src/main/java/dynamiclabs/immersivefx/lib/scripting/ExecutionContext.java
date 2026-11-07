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

package dynamiclabs.immersivefx.lib.scripting;

import dynamiclabs.immersivefx.lib.Lib;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.lib.scripting.expr.Bindings;
import dynamiclabs.immersivefx.lib.scripting.expr.Expression;
import dynamiclabs.immersivefx.lib.scripting.expr.ExpressionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ExecutionContext {

    private static final IModLog LOGGER = Lib.LOGGER;

    private static final String ERROR_RESULT = "ERROR?";

    private final String contextName;
    private final Bindings bindings = new Bindings();
    private final ObjectArray<VariableSet<?>> variables = new ObjectArray<>(8);
    private final Map<String, Expression> compiled = new HashMap<>();
    private final Expression error;

    public ExecutionContext(@Nonnull final String contextName) {
        this.contextName = contextName;
        this.error = Expression.compile("'<ERROR>'", this.bindings);
        this.bindings.put("lib", new LibraryFunctions());

        Lib.LOGGER.info("Script engine provided for %s: built-in expression evaluator", contextName);
    }

    public void put(@Nonnull final String name, @Nullable final Object obj) {
        this.bindings.put(name, obj);
    }

    public void add(@Nonnull final VariableSet<?> varSet) {
        if (this.bindings.isDefined(varSet.getSetName()))
            throw new IllegalStateException(String.format("Variable set '%s' already defined!", varSet.getSetName()));

        this.variables.add(varSet);
        this.bindings.put(varSet.getSetName(), varSet.getInterface());
    }

    public String getName() {
        return this.contextName;
    }

    public void update() {
        this.variables.forEach(VariableSet::update);
    }

    public boolean check(@Nonnull final String script) {
        final Optional<Object> result = eval(script);
        return result.isPresent() && "true".equalsIgnoreCase(result.get().toString());
    }

    @Nonnull
    public Optional<Object> eval(@Nonnull final String script) {
        Expression func = this.compiled.get(script);
        if (func == null) {
            func = makeFunction(script);
            this.compiled.put(script, func);
        }

        try {
            final Object result = func.evaluate();
            return Optional.ofNullable(result);
        } catch (@Nonnull final Throwable t) {
            LOGGER.error(t, "Error execution script: %s", script);
            this.compiled.put(script, this.error);
        }

        return Optional.of(ERROR_RESULT);
    }

    @Nullable
    public String validate(@Nonnull final String script) {
        try {
            Expression.compile(script, this.bindings);
            return null;
        } catch (@Nonnull final ExpressionException ex) {
            return ex.getMessage();
        }
    }

    @Nonnull
    private Expression makeFunction(@Nonnull final String script) {
        try {
            return Expression.compile(script, this.bindings);
        } catch (@Nonnull final Throwable t) {
            LOGGER.error(t, "Error compiling script: %s", script);
        }
        return this.error;
    }

}
