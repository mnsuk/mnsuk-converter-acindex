# mnsuk-converter-acindex

This converter sends text/plain documents to the Watson Explorer Content Analytics' annotation pipeline from a conversion pipeline in Engine for indexing in the Content Analytics server.

## Building

Copy the following jar files from your WEX lib/java directory into the lib directory of the project. 
    
* connectors-x.jar  (tested with 8.8.2-v0)
* converters-x.jar (tested with 3.2.3)
* java-xml-utils-x.jar (tested with 1.0.0)
* jpf-x.jar (tested with 1.5)
* libmisc-x.jar (tested with 8.8.2-v0)
* log4j-x.jar (tested with 1.2.14)
* slf4j-api-x.jar (tested with 1.6.1)
* slf4j-log4j12-.x.jar (tested with 1.6.1)
* vcrypt-java-x.jar (tested with 4.1.0)
    
Copy the following open source jar files from usual sources into the lib directory of the project.
    
* commons-codec-1.3.jar
* commons-httpclient-3.1.jar
* commons-logging-1.1.1.jar
* json-20140107.jar
    
The following legacy ICA REST api libraries are included, they'll get replaced in due course.
* ica-rest-api-nlp.jar
* ica-rest-api-document-add.jar
    
Run 'ant package' from the root of the project directory. 
    
## Deployment
    
1. Copy the mnsuk-converter-acindex-x.x.x-distrib.zip file from the target folder to the root of your Engine install dir
1. Unzip that archive
1. In the Engine Admin Tool, navigate to Management -> Installation -> Repository and click 'unpack' to add the converters's xml node to the repository.
1. Add the node "MNSUK Content Analytics Text Document Add to Index" like any other converter to the collection of your choice.


    
    

