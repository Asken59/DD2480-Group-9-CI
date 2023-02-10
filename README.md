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

## Implementation of compile, test and notify

### Compilation
Compilation was implemented by calling the "mvn compile" command in the repository directory and then capturing and
parsing the output of the command to determine if the compilation failed or succeeded. The testing was done by automated
unit tests that run the method on two subprojects. One of these subproject can not be compiled while the other can be
compiled the tests will pass if the method gives the correct output. 

### Test
Testing was implemented by calling the "mvn test" command in the repository directory and then capturing and
parsing the output of the command to determine if the tests failed or succeeded. The testing was done by automated
unit tests that run the method on two subprojects. One of these subprojects has tests that fail while the other has
tests that pass. The automated tests will pass if the method gives the correct output. 

### Notify
Notification was implemented trough GitHub commit status. The server makes calls to GitHubs' REST api with the relevant 
data. The functionality was tested by pushing numerous commits and checking so that the server set the correct status.
All possible variations of commits e.g. those that did not compile, compiled but failed tests etc. were tested.


## Contributions
All members of group 9 have contributed to this project, either through direct commits or by pair programming. 
Due to the size of this project being quite small, especially code related, pair programming was utilized to a greater deal than in the previous project *DECIDE*.

## Essence of the team
We evaluated that according to the essence checklist (p. 52), we are working on the "Adjourned" level. This is because we fulfill all the checklist items before "Adjourned" in addition to the ones directly linked to "Adjourned". The teams responsibility have passed on and been fulfilled and the team will no longer be putting effort into completing the mission. The team is also available for assignments to other teams.