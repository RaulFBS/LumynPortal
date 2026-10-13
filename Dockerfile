# 1. Descarga un servidor con Apache Tomcat 10.1 y Java 21 preinstalados
FROM tomcat:10.1-jdk21-temurin

# 2. Limpia las páginas de bienvenida por defecto de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# 3. Toma el archivo .war que compiló NetBeans y lo pone como página principal (ROOT)
COPY dist/LumynPortalWEB.war /usr/local/tomcat/webapps/ROOT.war

# 4. Abre el puerto 8080 e inicia el servidor
EXPOSE 8080
CMD ["catalina.sh", "run"]
