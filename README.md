# SupportFlow
Aplicación móvil Android desarrollada para la gestión de tickets de soporte técnico. Su objetivo es facilitar el registro, asignación, seguimiento y resolución de incidencias mediante un sistema organizado por roles. La aplicación permite que usuarios finales reporten problemas, que técnicos atiendan los tickets asignados y que administradores supervisen todo el flujo de soporte.

## Funcionalidades principales
### USER
El usuario puede:
-Registrarse e iniciar sesión.
-Verificar su cuenta mediante correo electrónico.
-Crear tickets de soporte.
-Seleccionar categoría y prioridad.
-Adjuntar evidencia fotográfica.
-Consultar sus propios tickets.
-Filtrar tickets por estado y asignación.
-Visualizar el técnico asignado.
-Consultar el estado del ticket.
-Enviar comentarios.
-Eliminar tickets mientras no hayan sido asignados.

### TECH
El técnico puede:
-Consultar únicamente los tickets que le han sido asignados.
-Visualizar el nombre del usuario que creó cada ticket.
-Filtrar tickets por categoría, estado, prioridad, fecha y usuario creador.
-Consultar la descripción y evidencia adjunta.
-Cambiar el estado del ticket.
-Marcar tickets como resueltos.
-Agregar comentarios técnicos.
-Consultar el historial de comentarios.

### ADMIN
El administrador cuenta con acceso a las funciones generales de gestión:
-Visualizar todos los tickets.
-Consultar el usuario creador de cada ticket.
-Filtrar tickets.
-Asignar técnicos.
-Cambiar el técnico asignado.
-Eliminar tickets.
-Consultar evidencia adjunta.
-Supervisar estados y prioridades.
-Administrar usuarios y roles.
-Visualizar reportes gráficos del sistema.

### Estados de los tickets
Los tickets pueden encontrarse en los siguientes estados:
-OPEN: ticket creado y pendiente de atención.
-IN_PROGRESS: ticket actualmente en proceso.
-RESOLVED: problema atendido y ticket resuelto.

### Prioridades
El usuario puede indicar el nivel de prioridad de la incidencia:
-LOW
-MEDIUM
-HIGH
-CRITICAL

## Tecnologías utilizadas
El proyecto fue desarrollado utilizando:
-Android Studio
-Java
-Kotlin
-XML
-Firebase Authentication
-Cloud Firestore
-Cloudinary
-Glide
-MPAndroidChart
-Git

## Objetivo del proyecto
SupportFlow fue desarrollado con el propósito de implementar una solución móvil que permita administrar de manera estructurada el ciclo completo de atención de incidencias.

El proyecto integra conceptos relacionados con:
desarrollo de aplicaciones Android; programación orientada a objetos; bases de datos en la nube; autenticación; gestión de usuarios y roles; almacenamiento de imágenes; interfaces gráficas; actualización de datos en tiempo real.

[Proyecto desarrollado con fines académicos y educativos.]
