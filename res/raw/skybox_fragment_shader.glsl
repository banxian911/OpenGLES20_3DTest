precision mediump float;
uniform samplerCube u_Texture;
varying vec3 v_TexCoordinate; 
void main() {
  gl_FragColor = textureCube(u_Texture, v_TexCoordinate);
}
