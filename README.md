# poc-app-social-media-sharing-photo

## Objective
- Build a Social Media Sharing Photo App (publish photos, tag photos, timeline, comments)



## Diagram
![Diagram](deepseek_mermaid_20250604_046c27.png)


## H2 Console
http://localhost:8080/h2-console
## Liquibase commands
```bash
#run validation
mvn liquibase:validate

#dry-run (show SQL without executing)
mvn liquibase:updateSQL

#execute changes
mvn liquibase:update

#rollback changes
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```