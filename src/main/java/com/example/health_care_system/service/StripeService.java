package com.example.health_care_system.service;

import com.example.health_care_system.dto.PaymentRequest;
import com.example.health_care_system.dto.StripeResponse;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service

public class StripeService {

        @Value("${stripe.secretKey}")
        private String secretKey;

        @Value("${server.port}")
        private String serverPort;

        //stripe -API
        //-> productName , amount , quantity , currency
        //-> return sessionId and url



        public StripeResponse checkoutProducts(PaymentRequest productRequest) {
            // Set your secret key. Remember to switch to your live secret key in production!
            Stripe.apiKey = secretKey;

            // Create a PaymentIntent with the order amount and currency
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(productRequest.getName())
                            .build();

            // Create new line item with the above product data and associated price
            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(productRequest.getCurrency() != null ? productRequest.getCurrency() : "lkr")
                            .setUnitAmount(productRequest.getAmount())
                            .setProductData(productData)
                            .build();

            // Create new line item with the above price data
            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams
                            .LineItem.builder()
                            .setQuantity(productRequest.getQuantity())
                            .setPriceData(priceData)
                            .build();

            // Create new session with the line items
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl("http://localhost:" + serverPort + "/appointments/payment/success?session_id={CHECKOUT_SESSION_ID}")
                            .setCancelUrl("http://localhost:" + serverPort + "/appointments/payment/cancel")
                            .addLineItem(lineItem)
                            .build();

            // Create new session
            Session session = null;
            try {
                session = createSession(params);
            } catch (Exception e) {
                //log the error
                System.err.println("Stripe error: " + e.getMessage());
                e.printStackTrace();
                return StripeResponse
                        .builder()
                        .status("FAILED")
                        .message("Payment session creation failed: " + e.getMessage())
                        .sessionId(null)
                        .sessionUrl(null)
                        .build();
            }

            if (session == null) {
                return StripeResponse
                        .builder()
                        .status("FAILED")
                        .message("Payment session creation failed")
                        .sessionId(null)
                        .sessionUrl(null)
                        .build();
            }

            return StripeResponse
                    .builder()
                    .status("SUCCESS")
                    .message("Payment session created ")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();
        }

        /**
         * Extracted for testing so it can be spied and stubbed in unit tests.
         */
        protected Session createSession(SessionCreateParams params) throws Exception {
            return Session.create(params);
        }

    }
