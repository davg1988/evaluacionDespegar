set projectPath=C:\Users\dv250124\git\evaluacionDespegar\Despegar
cd\
cd %projectPath%
set classpath=%projectPath%\target\classes;%projectPath%\lib\*;
java org.testng.TestNG suite.xml
pause