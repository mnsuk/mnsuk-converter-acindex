# mnsuk-converter-acindex


    Copy the following jar files from your WEX lib/java directory into the lib directory of the project. 
    
    connectors-x.x.x-vx.jar
    converters-x.x.x.jar
    java-xml-utils-x.x.x.jar
    jpf-x.x.jar
    libmisc-x.x.x-vx.jar
    log4j-x.x.x.jar
    slf4j-api-x.x.x.jar
    slf4j-log4jx.x.x.jar
    vcrypt-java-x.x.x.jar
    
    Copy the following open source jar files from usual sources into the lib directory of the project.
    
    commons-codec-1.3.jar
    commons-httpclient-3.1.jar
    commons-logging-1.1.1.jar
    json-20140107.jar
    
    Get a copy of the wrapper libraries for the WCA REST api from: 
    ica-rest-api-nlp.jar
    ica-rest-api-document-add.jar
    
    Run 'ant package' from the root of the project directory. 
    
    Deployment
    
    1. Copy the mnsuk-converter-acindex-x.x.x-distrib.zip file from the target folder to the root of your Engine install dir
    2. Unzip that archive
    3. In the Engine Admin Tool, navigate to Management -> Installation -> Repository and click 'unpack' to add the converters's xml node to the repository.
    4. Add the node "MNSUK Content Analytics Text Document Add to Index" like any other converter to the collection of your choice.


    
    

