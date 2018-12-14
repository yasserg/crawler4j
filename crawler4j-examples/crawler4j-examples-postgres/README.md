A sample shows how to save crawled page into a JDBC repository.

Shamelessy grabbed with rzo1's permission, from [the original repo](https://github.com/rzo1/crawler4j-postgres-sample).

If your surefire tests are failing on Windows due to docker.exe or docker-compose.exe not found, then create a local ``.mvn/maven.config`` file at the project root (i.e two levels higher than this project folder) with the following contents (_note that the odd use of quotes here looks wrong but actually works, at least for Maven 3.5.4_):  

	"-Ddocker.location=C:\Program Files\Docker Toolbox\docker.exe "-Ddocker.compose.location=C:\Program Files\Docker Toolbox\docker-compose.exe

Make sure the paths are correct for your system.