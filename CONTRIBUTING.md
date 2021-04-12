## How to Release

* logon/login on github.com
* put your SSH key in your github profile and clone project with SSH
* logon/login on Central Nexus https://oss.sonatype.org/ 
* add new server `ossrh` in your Maven config file `~/.m2/settings.xml` with the Nexus credentials
* generate a GPG key `gpg2 --full-gen-key` and publish it `gpg2 --keyserver keyserver.ubuntu.com --send-keys YOURID` 
* create a branch, build it locally `make build`, have it approved and merge it (do not change the version in the pom)
* pull `master` branch and run `make release`
* Note: the project use github CI to verify the build
* In case of failure, delete the release/tag in the project, revert the POM and reclone the project

