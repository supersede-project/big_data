var fs = require('fs');

var dependencies = fs.readdirSync("lib-ext/")

var outWindows = "";
var outUNIX = "";
for (i = 0; i < dependencies.length; ++i) {
	var j = dependencies[i].search("lib-ext/")+8;
	var file = dependencies[i];
	var name = file.substr(0,file.search(".jar"));
	var first = "mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=lib-ext/";
	var second = " -DgroupId=";
	var third = " -DartifactId=";
	var fourth = " -Dversion=1 -Dpackaging=maven-plugin";

	if (name) {
		console.log(name);
		outWindows += "call "+first + file + second + name + third + name + fourth + "\n";
		outUNIX += first + file + second + name + third + name + fourth + "\n";		
	}
}

fs.writeFile("installDependenciesWindows.bat", outWindows, function(err) {
    if(err) return console.log(err);
    console.log("installDependenciesWindows.bat saved");
}); 

fs.writeFile("installDependenciesUnix.sh", outUNIX, function(err) {
    if(err) return console.log(err);
    console.log("installDependenciesUnix.sh saved");
}); 

var mvnDependencies = "";
for (i = 0; i < dependencies.length; ++i) {
	var file = dependencies[i];
	var name = file.substr(0,file.search(".jar"));
	
	var first = "<dependency><groupId>"
	var second = "</groupId><artifactId>"
	var third = "</artifactId><version>1</version></dependency>"

	if (name != ".svn") {
		mvnDependencies += first + name + second + name + third + "\n";
	}
}

fs.writeFile("mvnDependenciesToAdd.xml", mvnDependencies, function(err) {
    if(err) return console.log(err);
    console.log("mvnDependenciesToAdd.xml saved");
}); 
