# Continues Integration
This project is a continues integration server that will compile and test your GitHub repo. 
It will notify if a commit passes, through the commit status on GitHub. **Note** that for commit status notifications to work the server requires a GitHub token with the commit status read/write permissions to be entered at start.  
Logs of its builds will also be saved and accessible through the url <url-to-server>/index.html.

## Compiling and testing this repo
If you are a contributor to this repo or just want to compile from source, you may easily do so with Maven.
Note that this project uses Maven 3.8.0.

For compiling, either use `$ mvn compile` to test if compilation is possible or `$ mvn package` to build a executable .jar in the target/ folder.  
To execute and start the server you may use `$ mvn exec:java -Dexec.mainClass="group9.CI`.  
Cleaning up after package builds is easily done with `$ mvn clean`

For running the tests simply use `$ mvn test`.

## Contributions
All members of group 9 have contributed to this project, either through direct commits or by pair programming. 
Due to the size of this project being quite small, especially code related, pair programming was utilized to a greater deal than in the previous project *DECIDE*.

## Essence of the team