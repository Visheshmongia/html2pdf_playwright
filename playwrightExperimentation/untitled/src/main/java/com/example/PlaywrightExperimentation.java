package com.example;

import com.microsoft.playwright.*;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlaywrightExperimentation {
    public static void main(String[] args) {
        Page page;
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100));
            BrowserContext context = browser.newContext();
            page = context.newPage();

            // Request interception
            page.onRequest(request -> {
                try {
                    String urlString = request.url();
                    System.out.println("Intercepting request for URL: " + urlString);
                    String FILE_URL = "file:";

                    if (urlString.startsWith(FILE_URL)) {
                        Path realPath = Path.of(new URI(urlString));
                        if (Files.isSymbolicLink(realPath)) {
                            throw new Exception("Aborting request since this is a potential symlink");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            context.route("**/*", route -> {
                try {
                    String urlString = route.request().url();
                    System.out.println("Intercepting request for URL: " + urlString);
                    String FILE_URL = "file:";

                    if (urlString.startsWith(FILE_URL)) {
                        Path realPath = Path.of(new URI(urlString));
                        if (Files.isSymbolicLink(realPath)) {
                            route.abort();
                            throw new Exception("Aborting request since this is a potential symlink");
                        }
                    } else {
                        route.resume();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            System.out.println("Navigating to page");
            page.navigate("https://www.zeposia.com/content/commercial/us/zeposiadtc/en/understanding-uc.desktop.html?q0=dt&q1=di-s&q2=&q3=on&q4=r&q5=rfi&q6=ho&q7=ngb");

            try {
                page.pdf(new Page.PdfOptions().setPath(Paths.get("/Users/vmongia/Alpine/playwright/Output/haello.pdf")));
            } catch (PlaywrightException e) {
                System.out.println("Error: " + e.getMessage());
            } finally {
                System.out.println("Closing the objects");
                if (page != null) page.close();
                if (context != null) context.close();
                if (browser != null) browser.close();
            }
        }
    }
}
