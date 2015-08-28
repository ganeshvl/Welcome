
CODE SIGNING SETUP
------------------

Code signing is done with the maven-jarsigner-plugin as part of the build.
To setup the configuration for EntradaHealth do the following ...


1) Checkout a copy of the android-signing-keys.

   $ git clone = Entrada Git URL

   i). In that repo you will found keystore folder.

2) Add gradle.properties file in root of the project.  The file should look like follows
   replacing /path/to/keystore/entrada.keystore with the correct path.

   releaseKeystore=/path/to/keystore/entrada.keystore
   releaseStorePassword= need to update
   releaseKeyAlias= need to update
   releaseKeyPassword= need to update


3) Now you can build.

   $ ./gradlew build 

