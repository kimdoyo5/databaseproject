# About
View under Project/Instructions.pdf.

# Setup Instructions
1. Follow the steps under Project/Project-EnvSetup-Instructions.pdf to complete the initial setup
2. Follow these steps to complete the initial setup for SSH access from local machine to Google Cloud Compute Engine: https://medium.com/@riyasyash/setting-up-ssh-access-from-local-machine-to-google-cloud-compute-engine-c6f87ea16d3a (access to VM is useful for directly seeing the database)
3. Follow the steps in this post to allow for remote connection to the database: https://piazza.com/class/lvbvuhg7ewd2a7/post/113

~To connect to Google Cloud Compute Engine and run the Java program, complete the following steps:~

~1. Download this Replit project~

~2. From the local machine, upload the java folder to /data/ in the VM using `scp`~

   ~For example, type `scp -r java [username]@[IP address]:/data/` once in the directory with the java folder.~
   
~3. Type `ssh -i id_rsa [username]@[IP address]` into the terminal~

~4. Confirm if prompted~

~5. Install dependencies: `cd /data/java/; ./InstallDeps.sh` (may need to add execute permissions using `chmod`)~

~6. Set up the Java project with Maven: `./SetupProject.sh` (may need to add execute permissions using `chmod`)~

~7. Compile the program and run it: `./BuildAndRun.sh` (may need to add execute permissions using `chmod`)~

~8. Exit SSH with `^d`~

You may need to do other steps, which could include some of the following:
1. If you created an additional class (Java file), make sure to add it to SetupProject.sh
2. Check if database is active if there is an error connecting to the database
3. If the VM restarted, type `sudo mount /dev/sdb /data` once in the VM
4. Make sure historical data is loaded into Stocks and DailyStocks relations (.csv files located under Project folder)

**Follow below for future access.**

1. Go to the shell in Replit
2. Type `cd src/main/java; ./SetupProject.sh; ./BuildAndRun.sh`