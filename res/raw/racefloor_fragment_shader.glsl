precision mediump float;
uniform sampler2D u_Texture;
uniform vec4 vColor;
varying vec2 v_TexCoordinate; 
void main() {
  // gl_FragColor = vColor;
  gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
}
