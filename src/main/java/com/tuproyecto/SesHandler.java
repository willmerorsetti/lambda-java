package com.tuproyecto;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

public class SesHandler implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event s3event, Context context) {
        // 1. Extraer datos del primer registro del evento S3
        S3EventNotificationRecord record = s3event.getRecords().get(0);
        String bucketName = record.getS3().getBucket().getName();
        String fileName = record.getS3().getObject().getUrlDecodedKey();

        context.getLogger().log("Procesando archivo: " + fileName + " de bucket: " + bucketName);

        // 2. Cliente SES
        SesClient client = SesClient.builder().region(Region.US_EAST_1).build();

        // 3. Construir mensaje
        String mensaje = "¡Hola! Se ha subido un nuevo archivo: " + fileName + " al bucket: " + bucketName;

        SendEmailRequest request = SendEmailRequest.builder()
            .destination(Destination.builder().toAddresses("willferorsetti@gmail.com").build())
            .message(Message.builder()
                .subject(Content.builder().data("Alerta S3 - JAVA").build())
                .body(Body.builder().text(Content.builder().data(mensaje).build()).build())
                .build())
            .source("willferorsetti@gmail.com")
            .build();

        try {
            client.sendEmail(request);
            return "Email enviado con éxito desde Java";
        } catch (Exception e) {
            return "Error enviando email: " + e.getMessage();
        }
    }
}
