What is Finger for EVE (FFE)?

Finger for EVE is an intelligence management tool designed to track characters, such as hostile FCs, cyno alts, super caps, titans, etc. Following an incident where a capital fleet almost got hot dropped due to an intel sharing failure, FFE was built to provide an easier, faster, safer and more secure way to share that type of intelligence. A screenshot of the application <a href="http://wiki.eve-id.net/images/Ffe_windows.png">can be found here.</a>

Who is FFE for?

Out of the box, FFE does NOT provide EVE API driven user registration. It is designed for alliances that already have infrastructure to handle user authentication and authorization (e.g. single sign on), and as such exposes a Java API that allows you to attach it to your existing single sign on system. You will need to have a sys admin who can setup and configure a Java (GWT) web app, MySQL database, and a developer that is familiar with Java and your single sign on system to write an authentication connector.

How to install/configure/run FFE:

1. Deploy the app's WAR to Tomcat, and let the root of the deployed war be known as $WAR_BASE. No other app containers will be supported, but it should be trivial for an experienced Java developer to run it on another container.

2. Carefully read EVERYTHING in the annotated AuthProvider interface in $APP_SOURCE/src/net/eve/finger/server, write a suitable AuthProvider for your infrastructure, and place the resulting jar in $WAR_BASE/WEB-INF/lib. Note that two reference auth provider implementations are provided in the package if you need additional guidence. Also, note that if users are eligible for multiple groups, you should probably default them to the one with the highest power (see step 7 for more details).

4. Set the app to use your auth provider by setting the auth provider entries in $WAR_BASE/META-INF/context.xml and $WAR_BASE/WEB-INF/web.xml to point to your auth provider class.

5. On your MySQL server, execute $APP_SOURCE/db/db.sql to create the database. You will need to create a user with the proper permissions for the next step.

6. Set the database details in META-INF/context.xml.

7. Create the access groups used by your auth provider in the DB's tblAccessGroups table. Note that the power column determines the group hierarchy. Groups with a given power level can view intel entries with their power level, AND entries below their power level. Any entry created by a user will default to their power level. In reality, users will just see a list of groups ordered by power level, and can set the restrictions on their intel accordingly.

8. Set the URL of your instance of FFE on line 33 of $WAR_ROOT/Net_eve_finger.html so that trust is properly requested when clients are using the IGB.

9. Test the system by having a user login. If you did everything correctly, it should be working.

How do I read the app's security log?

Because the app is no longer in use, and logging was the last feature added, there is no UI to view it. All log entries are stored in the DB's tblLog table, and should be easy enough for any sys admin to read, and / or a developer to write a simple web UI to view.

What if I want to add a feature?

Eclipse and the Google Plugin for Eclipse is the recommended development environment.
