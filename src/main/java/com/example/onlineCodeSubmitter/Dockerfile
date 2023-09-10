# Use the JDK 11 base image
FROM openjdk:11

# Create a directory in the image for the JUnit JAR files
RUN mkdir -p /home/app

# Copy the JUnit JAR files from your local machine to the image
COPY  C:\\JUnit\*.jar /home/app/

# Set the working directory
WORKDIR /home/app
