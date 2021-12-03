package com.murkitty.parking;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.util.HashMap;
import java.util.Map;
public class ShaderHelper {

public static final ShaderProgram FONT_GREEN = getFontShader(Color.BLUE);
public static final ShaderProgram FONT_LIGHT_GRAY = getFontShader(Color.BLUE);
public static final ShaderProgram FONT_RED = getFontShader(Color.BLUE);
public static final ShaderProgram FONT_YELLOW = getFontShader(Color.BLUE);
private static final Map<ColorEffect, ShaderProgram> map = new HashMap<ColorEffect, ShaderProgram>();

public static ShaderProgram getShader(float red, float green, float blue, float alpha) {
	if(red < 0 || red > 4) {
		App.log.error("red < 0 || red > 4");
	}
	if(green < 0 || green > 4) {
		App.log.error("green < 0 || green > 4");
	}
	if(blue < 0 || blue > 4) {
		App.log.error("blue < 0 || blue > 4");
	}
	if(alpha < 0 || alpha > 4) {
		App.log.error("alpha < 0 || alpha > 4");
	}
	ColorEffect colorEffect = new ColorEffect(red, green, blue, alpha);
	if(!map.containsKey(colorEffect)) {
		ShaderProgram shader = createShader(red, green, blue, alpha);
		if(!shader.isCompiled()) {
			App.log.error("shader not compiled");
		}
		map.put(colorEffect, shader);
	}
	return map.get(colorEffect);
}
private static ShaderProgram createShader(float red, float green, float blue, float alpha) {
	String vertexShader = "attribute vec4 a_position;    \n" +
					"attribute vec4 a_color;\n" +
					"attribute vec2 a_texCoord0;\n" +
					"uniform mat4 u_projTrans;\n" +
					"varying vec4 v_color;" +
					"varying vec2 v_texCoords;" +
					"void main()                  \n" +
					"{                            \n" +
					"   v_color = vec4(" + red + ", " + green + ", " + blue + ", " + alpha + "); \n" +
					"   v_texCoords = a_texCoord0; \n" +
					"   gl_Position =  u_projTrans * a_position;  \n" +
					"}                            \n";
	String fragmentShader = "#ifdef GL_ES\n" +
					"precision mediump float;\n" +
					"#endif\n" +
					"varying vec4 v_color;\n" +
					"varying vec2 v_texCoords;\n" +
					"uniform sampler2D u_texture;\n" +
					"void main()                                  \n" +
					"{                                            \n" +
					"  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
					"}";
	ShaderProgram result = new ShaderProgram(vertexShader, fragmentShader);
	if(false) {
		result.dispose();
	}
	return result;
}

private static ShaderProgram getFontShader(Color color) {
	//todo uniform color vector3
	//https://github.com/libgdx/libgdx/wiki/Distance-field-fonts
	String vertexShader = "attribute vec4 a_position;    \n" +
					"attribute vec4 a_color;\n" +
					"attribute vec2 a_texCoord0;\n" +
					"uniform mat4 u_projTrans;\n" +
					"varying vec4 v_color;\n" +
					"varying vec2 v_texCoords;\n" +
					"void main()                  \n" +
					"{                            \n" +
					"   v_color = vec4(" + color.r + ", " + color.g + ", " + color.b + ", 1); \n" +
					"   v_texCoords = a_texCoord0; \n" +
					"   gl_Position =  u_projTrans * a_position;  \n" +
					"}                            \n";
	String fragmentShader = "#ifdef GL_ES\n" +
					"precision mediump float;\n" +
					"#endif\n" +
					"varying vec4 v_color;\n" +
					"varying vec2 v_texCoords;\n" +
					"uniform sampler2D u_texture;\n" +
					"const float outlineDistance = 0.2;\n" +//from 0.0 to 0.5
					"const vec4 outlineColor = vec4(0,0,0,1);\n" +
					"const float smoothing = 1.0/16.0;\n" +
					"void main()                                  \n" +
					"{                                            \n" +
					"  float distance = texture2D(u_texture, v_texCoords).a;\n" +
					"  float outlineFactor = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);\n" +
					"  vec4 color = mix(outlineColor, v_color, outlineFactor);\n" +
					"  float alpha = smoothstep(outlineDistance - smoothing, outlineDistance + smoothing, distance);\n" +
					"  gl_FragColor = vec4(color.rgb, color.a * alpha);\n" +
//					"  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
					"}";
	if(false) {
		//	Adding a drop shadow
		//	Here, we sample the texture a second time, slightly offset from the first.
		// The second application gets a lot more smoothing applied to it, and is rendered "behind" the actual text.
		fragmentShader = "#ifdef GL_ES\n" +
						"precision mediump float;\n" +
						"#endif\n" +
						"varying vec4 v_color;\n" +
						"varying vec2 v_texCoords;\n" +
						"uniform sampler2D u_texture;\n" +
						"const float smoothing = 0.3;\n" + // Between 0 and 0.5
						"const vec2 shadowOffset = vec2(2,2);    \n" + // Between 0 and spread / textureSize
						"const float shadowSmoothing = 0.4;\n" + // Between 0 and 0.5
						"const vec4 shadowColor = vec4(1,0,0,1);\n" +
						"void main()                                  \n" +
						"{                                            \n" +
						"  float distance = texture2D(u_texture, v_texCoords).a;\n" +
						"  float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);\n" +
						"  vec4 text = vec4(v_color.rgb, v_color.a * alpha);\n" +
						"  float shadowDistance = texture2D(u_texture, v_texCoords - shadowOffset).a;\n" +
						"  float shadowAlpha = smoothstep(0.5 - shadowSmoothing, 0.5 + shadowSmoothing, shadowDistance);\n" +
						"  vec4 shadow = vec4(shadowColor.rgb, shadowColor.a * shadowAlpha);" +
						"  gl_FragColor = mix(shadow, text, text.a);\n" +
						"}";
	}
	return new ShaderProgram(vertexShader, fragmentShader);
}
public static class ColorEffect {
	public static final float MIN = 0;
	public static final float MAX = 4;
	public static final int gradations = 64;
	private final int hash;
	public ColorEffect(float r, float g, float b, float a) {
		if(r < MIN || r > MAX) {
			App.log.error("r < MIN || r > MAX");
		}
		if(g < MIN || g > MAX) {
			App.log.error("g < MIN || g > MAX");
		}
		if(b < MIN || b > MAX) {
			App.log.error("b < MIN || b > MAX");
		}
		if(a < MIN || a > MAX) {
			App.log.error("a < MIN || a > MAX");
		}
		byte rb = (byte) ((r - MIN) / (MAX - MIN) * gradations);
		byte gb = (byte) ((g - MIN) / (MAX - MIN) * gradations);
		byte bb = (byte) ((b - MIN) / (MAX - MIN) * gradations);
		byte ab = (byte) ((a - MIN) / (MAX - MIN) * gradations);
		hash = (rb << 24) + (gb << 16) + (bb << 8) + ab;
	}
	@Override
	public int hashCode() {
		return hash;
	}
	@Override
	public boolean equals(Object obj) {
		return this.hashCode() == obj.hashCode();
	}
}
}
