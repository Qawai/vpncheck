#!/data/data/com.termux/files/usr/bin/bash
set -e
cd "$HOME/apkbuild"

rm -rf classes dex app-unsigned.apk
mkdir -p classes dex

echo "[0/6] иконка..."
if [ ! -f res/mipmap-xxxhdpi/ic_launcher.png ]; then
  javac -d . IconGen.java 2>/dev/null || true
  java -cp . IconGen res
fi

echo "[1/6] javac..."
javac -source 1.8 -target 1.8 -classpath android.jar \
  -d classes src/com/qawai/vpncheck/MainActivity.java 2>&1 | grep -v "bootstrap class path" || true

echo "[2/6] d8 -> dex..."
d8 --release --lib android.jar --output dex $(find classes -name '*.class')

echo "[3/6] aapt..."
aapt package -M AndroidManifest.xml -S res -I android.jar -F app-unsigned.apk

echo "[4/6] zip + подпись..."
(cd dex && zip -q ../app-unsigned.apk classes.dex)

if [ ! -f key.jks ]; then
  keytool -genkeypair -keystore key.jks -keyalg RSA -keysize 2048 \
    -validity 10000 -storepass pass1234 -keypass pass1234 \
    -alias vpncheck -dname "CN=vpncheck" -noprompt
fi

apksigner sign --ks key.jks --ks-pass pass:pass1234 \
  --out "$HOME/vpncheck.apk" app-unsigned.apk

echo "[5/6] проверка..."
apksigner verify --verbose "$HOME/vpncheck.apk" | grep -E "Verified using|Signer"
ls -lh "$HOME/vpncheck.apk"

