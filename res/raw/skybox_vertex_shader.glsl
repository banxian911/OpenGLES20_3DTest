uniform mat4 uMVPMatrix;
attribute vec4 vPosition;
varying vec3 v_TexCoordinate;

void main() {
  gl_Position = uMVPMatrix * vPosition;
  v_TexCoordinate = vec3(vPosition);
}
