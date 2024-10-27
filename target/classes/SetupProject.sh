mvn archetype:generate -DgroupId=cs.toronto.edu -DartifactId=pgsample -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
cp Main.xml pgsample/pom.xml

cp Main.java pgsample/src/main/java/cs/toronto/edu/
cp TableCreator.java pgsample/src/main/java/cs/toronto/edu/
cp AccountManager.java pgsample/src/main/java/cs/toronto/edu/
cp SQLUtilities.java pgsample/src/main/java/cs/toronto/edu/
cp FriendManager.java pgsample/src/main/java/cs/toronto/edu/
cp PortfolioManager.java pgsample/src/main/java/cs/toronto/edu/
cp StockListManager.java pgsample/src/main/java/cs/toronto/edu/
cp StockManager.java pgsample/src/main/java/cs/toronto/edu/
cp Colours.java pgsample/src/main/java/cs/toronto/edu/
cp ReviewManager.java pgsample/src/main/java/cs/toronto/edu/

rm pgsample/src/main/java/cs/toronto/edu/App.java
