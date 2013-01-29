cd ./include/OpenCCVServer
./rebar get-deps compile
cp ../compile_flags.hrl ./deps/proper/include
./rebar get-deps compile
cd ../..
rm -rf ./plugin
mkdir plugin
cd plugin
cp -r ../include/OpenCCVServer/plugin/* ./

cd ../src

echo "Please enter the hostname or IP address of the server that will host OpenCCV:"
read servername

occvhead=`cat ./SocketConnection.java.head`
occvtail=`cat ./SocketConnection.java.tail`
socketconnectiondotjava=$occvhead$servername$occvtail

echo -e "$socketconnectiondotjava" > ./SocketConnection.java

cd ..
rm -rf ./bin
mkdir bin
javac -classpath ./lib/jogl.jar ./src/*.java ./src/JSON/*.java -d ./bin
cd bin
jar cf OpenCCV.jar ./*
cp ./OpenCCV.jar ../plugin/open_ccv/priv/www
cd ../plugin/open_ccv/priv/www
echo "Now we will create a key for OpenCCV. Don't forget the password you supply during the prompt, you will be asked for it severl times over."
./make_key.sh $servername
echo "Please re-enter the password:"
read passphrase
echo "Signing jars..."
./sign_jar.sh $passphrase $servername
echo "Generating JNLP file..."
./make_jnlp.sh $servername
echo "Done."
