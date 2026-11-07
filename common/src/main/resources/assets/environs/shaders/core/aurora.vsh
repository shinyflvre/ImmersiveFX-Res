#version 330
/*
 * ImmersiveFX
 * Contains third party code, see the original notice below.
 * Edited by Shinyflvres and contributors, 2026.
 */
#extension GL_ARB_separate_shader_objects : require

//
// Created by Mattenii
// https://www.shadertoy.com/view/MsjfRG
// Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0)
//
// Shader was modified from the original to split it into separate
// vertex and frag shader scripts and adapted to fit the shader
//

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;

layout(location = 0) out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
}
