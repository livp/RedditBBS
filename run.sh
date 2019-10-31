#!/bin/sh
mvn compile && mvn exec:java -Dexec.mainClass="livia.RedditBBS"
