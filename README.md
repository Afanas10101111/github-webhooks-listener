[![Codacy Badge](https://app.codacy.com/project/badge/Grade/9373a3c0bf8d435591334d98e6764a1b)](https://app.codacy.com/gh/Afanas10101111/github-webhooks-listener/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

Github Webhooks Listener
========
Run this service on your Linux server where your applications are deployed, and configure a webhook in your github
repository to run deployment scripts on push events. Hand made CI/CD :)

### Usage
1. execute mvn clean package
2. place the generated jar-file in the directory from which you want to start the service
3. start the service by executing <code>nohup java -Dserver.port=8888 -Dscript.catalog_name=DIR -Dsecret.token=TOKEN -jar github-webhooks-listener-1.0.0.jar</code> where 
    * 8888 - service port 
    * DIR - name of the script directory (will be created at startup in the application directory if it does not exist) 
    * TOKEN - secret, configured for webhook (<a href="https://docs.github.com/ru/webhooks-and-events/webhooks/securing-your-webhooks">about secret</a>)
4. place script(s) to the script directory (scripts should have sh extension)
5. configure webhook 
    * set Payload URL http://your-server:8888/gwl/v1/push/main/scriptName ("main" and "scriptName" are PathVariables)
    * choose application/json Content type
    * set Secret (equals TOKEN)
    * choose Just the push event 
6. push event in main branch will execute scriptName.sh located in the script directory
