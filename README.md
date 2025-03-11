# Orderly Application Setup Guide

## Required Dependencies

The application requires the MySQL JDBC driver to connect to the database:

1. Create a `lib` folder in the project root directory:

2. Download the MySQL Connector/J (version 9.2.0) from the MySQL website or Maven Repository

3. Add the `mysql-connector-j-9.2.0.jar` file to the `lib` folder

4. Make sure the JAR file is included in your classpath when compiling and running the application

## Database Setup

SQL setup instructions can be found in `src/main/java/sql/sql_setup.sql`

The application will automatically create all required tables through the DatabaseManager when first run.

## Notes

- For production environments, replace the placeholder password with a strong, unique password
- Remember to set up your `.env` file with the database connection information
