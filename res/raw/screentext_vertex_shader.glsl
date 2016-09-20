uniform mat4 uMVPMatrix;
attribute vec4 vPosition;
attribute vec2 a_TexCoordinate;
varying vec2 v_TexCoordinate;

void main() {
  gl_Position = uMVPMatrix * vPosition;
  v_TexCoordinate = a_TexCoordinate;
}
