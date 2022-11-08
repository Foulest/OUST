package net.foulest.oust;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OUST {

    public static List<String> results = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scnr = new Scanner(System.in);

        System.out.println();
        System.out.println("OUST: Official USA Scraping Tool");
        System.out.println("by Foulest#0001");

        // Sets the term to search for.
        System.out.println();
        System.out.print("Enter first and last name: ");
        String fullName = scnr.nextLine();
        fullName = fullName.trim();
        Pattern pattern = Pattern.compile(" ");
        Matcher matcher = pattern.matcher(fullName);

        // Removes all non-alphanumeric characters from the name.
        fullName = fullName.replaceAll("[^a-zA-Z0-9 -]", "");

        // Ignores names with invalid space counts.
        if (matcher.results().count() != 1) {
            System.out.println();
            System.out.println("Error: Invalid name; both first and last names are needed.");
            System.out.println("Example: John Smith (not John, or John Smith Jr)");
            return;
        }

        // Splits the full name into first & last name.
        String[] splitName = fullName.split(" ");
        String firstName = splitName[0];
        String lastName = splitName[1];

        // Sets the location to search for.
        System.out.print("Enter city (optional): ");
        String location = scnr.nextLine();

        // Searches for the search term.
        System.out.println();
        System.out.println("Searching for: " + fullName + (location.equals("") ? "" : " in " + location));

        // Searches OfficialUsa.com for the search term.
        searchForTerm(firstName, lastName, location);

        // Removes duplicates from results.
        List<String> trimmedResults = new ArrayList<>(results.stream().distinct().toList());
        Collections.sort(trimmedResults);

        // Sets the results found.
        if (trimmedResults.isEmpty()) {
            System.out.println();
            System.out.println("No results found.");
        } else {
            System.out.println();
            System.out.println("Results:");
            trimmedResults.forEach(System.out::println);
        }
    }

    public static void searchForTerm(String firstName, String lastName, String location) {
        // Defines the search query depending on our search engine.
        String query = "https://officialusa.com/names/" + firstName + "-" + lastName;

        // Grabs the website's source code.
        System.out.println("GET -> " + query);
        String websiteSrc = getWebsiteSrc(query);

        // Returns if the website is blocking our connection.
        if (websiteSrc.equals("Blocked")) {
            System.out.println("BLOCKED -> " + query);
            return;
        }

        // Returns if website results could not be found.
        if (websiteSrc.equals("Not found")) {
            System.out.println("NOT FOUND -> " + query);
            return;
        }

        // Trim the source code to only include personal information.
        String srcTrimmed = websiteSrc.substring(websiteSrc.indexOf("<div class=\"persons detail-block__main-items\">"),
                        websiteSrc.indexOf("<div class=\"detail-block__main-item detail-block__main-item-summary\">"))
                .concat("</div>");

        // Sets the results variables.
        int resultsFound = 0;
        List<List<String>> printResults = new ArrayList<>();

        // Converts the HTML code into a document.
        Document document = Jsoup.parse(srcTrimmed);
        Elements elements = document.body().select("*");

        // Parses through all the found elements.
        // If an element's id matches our regex, it's a match.
        for (Element element : elements) {
            if (element.id().toLowerCase().matches(lastName.toLowerCase() + "-......")) {
                Elements children = element.children();
                List<String> pastAddress = new ArrayList<>();
                List<String> phoneNumbers = new ArrayList<>();
                List<String> possibleConnections = new ArrayList<>();
                List<String> alternativeNames = new ArrayList<>();
                String fullName = null;
                String summary;
                String age = null;
                String birthday = null;
                String emailAddress = null;
                String address = null;
                String homeInfo = null;
                String bedBath = null;
                String lotSize = null;
                String floorSize = null;
                String parcelId = null;
                String lastSaleDate = null;
                String lastSalePrice = null;
                String county = null;
                String neighborhood = null;
                String fips = null;
                String latLong = null;

                // Parses through the first 'ul' section.
                if (children.select("ul").size() >= 1) {
                    List<String> sectionOne = children.select("ul").get(0).select("li").eachText();

                    // Sets the main address on file.
                    address = sectionOne.get(0);

                    // Basic location check.
                    if (!location.equals("")) {
                        if (address != null) {
                            if (!address.toLowerCase().contains(location.toLowerCase())) {
                                continue;
                            }
                        }
                    }

                    // Adds a positive result found to the counter.
                    ++resultsFound;

                    for (String line : sectionOne) {
                        // Sets the county on file.
                        if (line.contains("County: ")) {
                            county = line.replaceAll("County: ", "");
                        }

                        // Sets the neighborhood on file.
                        if (line.contains("Neighborhood: ")) {
                            neighborhood = line.replaceAll("Neighborhood: ", "");
                        }

                        // Sets the home info on file.
                        if (line.contains("Single Family") || line.contains("Townhouse")
                                || line.contains("Attached Garage")
                                || line.contains(", Garage")
                                || line.contains(" spaces, ")
                                || line.contains(" sqft garage")) {
                            homeInfo = line;
                        }

                        // Sets the bathroom & bedroom info on file.
                        if (line.contains(" bedrooms, ")) {
                            bedBath = line;
                        }

                        // Sets the lot size info on file.
                        if (line.contains("Lot Size - ")) {
                            lotSize = line.replaceAll("Lot Size - ", "");
                            lotSize = lotSize.replaceAll(", .*", "");
                        }

                        // Sets the floor size info on file.
                        if (line.contains("Floor Size - ")) {
                            floorSize = line.replaceAll("Floor Size - ", "");
                            floorSize = floorSize.replaceAll(".*, ", "");
                        }

                        // Sets the parcel id on file.
                        if (line.contains("Parcel ID#")) {
                            parcelId = line.replaceAll("Parcel ID# ", "");
                        }

                        // Sets the last sale info on file.
                        if (line.contains("Last Sale ")) {
                            lastSaleDate = line.replaceAll("Last Sale ", "");
                            lastSaleDate = lastSaleDate.replaceAll(" - .*", "");
                            lastSalePrice = line.replaceAll(".* - Price ", "");
                        }

                        // Sets the FIPS info on file.
                        if (line.contains("FIPS: ")) {
                            fips = line.replaceAll("FIPS: ", "");
                        }

                        // Sets the latitude and longitude info on file.
                        if (line.contains("Latitude, Longitude: ")) {
                            latLong = line.replaceAll("Latitude, Longitude: ", "");
                        }

                        // Sets any possible connections on file.
                        if (line.contains("Possible connections via")) {
                            line = line.replaceAll("Possible connections via .* - ", "");

                            if (line.contains(", ")) {
                                Collections.addAll(possibleConnections, line.split(", "));
                            } else {
                                possibleConnections.add(line);
                            }
                        }
                    }
                }

                // Parses through the second 'ul' section.
                if (children.select("ul").size() >= 2) {
                    List<String> sectionTwo = children.select("ul").get(1).select("li").eachText();

                    for (String line : sectionTwo) {
                        // Sets any possible connections on file.
                        if (line.contains("Possible connections via")) {
                            line = line.replaceAll("Possible connections via .* - ", "");

                            if (line.contains(", ")) {
                                Collections.addAll(possibleConnections, line.split(", "));
                            } else {
                                possibleConnections.add(line);
                            }
                        }

                        // Sets any past addresses on file.
                        if (line.matches(".*, .. .....$")) {
                            pastAddress.add(line);
                        }

                        // Sets any phone numbers on file.
                        if (line.matches("\\(...\\) ...-....+")) {
                            if (line.contains(", ")) {
                                Collections.addAll(phoneNumbers, line.split(", "));
                            } else {
                                phoneNumbers.add(line);
                            }
                        }
                    }
                }

                // Parses through the third 'ul' section.
                if (children.select("ul").size() >= 3) {
                    List<String> sectionThree = children.select("ul").get(2).select("li").eachText();

                    for (String line : sectionThree) {
                        // Sets any possible connections on file.
                        if (line.contains("Possible connections via")) {
                            line = line.replaceAll("Possible connections via .* - ", "");

                            if (line.contains(", ")) {
                                Collections.addAll(possibleConnections, line.split(", "));
                            } else {
                                possibleConnections.add(line);
                            }
                        }

                        // Sets any past addresses on file.
                        if (line.matches(".*, .. .....$")) {
                            pastAddress.add(line);
                        }

                        // Sets any phone numbers on file.
                        if (line.matches("\\(...\\) ...-....+")) {
                            if (line.contains(", ")) {
                                Collections.addAll(phoneNumbers, line.split(", "));
                            } else {
                                phoneNumbers.add(line);
                            }
                        }
                    }
                }

                // Parses through the 'h2' section.
                if (children.select("h2").size() >= 1) {
                    fullName = children.select("h2").text();
                }

                // Parses through the 'p' section.
                if (children.select("p").size() >= 1) {
                    summary = children.select("p").text();

                    // Sets any alternative names on file.
                    if (summary.contains("alternative name")) {
                        String altNameTrim = summary.substring(summary.indexOf(". "), summary.indexOf("are instances of the alternative"));
                        altNameTrim = altNameTrim.replace(". ", "");
                        Collections.addAll(alternativeNames, altNameTrim.split(", "));
                    }

                    // Sets any birthday and age information on file.
                    if (summary.contains("birthday on")) {
                        birthday = summary.substring(summary.indexOf("birthday on "), summary.indexOf("."));
                        birthday = birthday.replace("birthday on ", "");
                        age = summary.substring(summary.indexOf("celebrated "), summary.indexOf(" birthday"));
                        age = age.replaceAll("[^0-9.]", "");
                    }

                    // Sets any email address information on file.
                    if (summary.contains("message at their address")) {
                        emailAddress = summary.substring(summary.indexOf("message at their address"));
                        emailAddress = emailAddress.replace("message at their address ", "");
                    }
                }

                // Sets the print result string list variable.
                List<String> printResult = new ArrayList<>();

                // Adds a top divider containing the element ID to help separate results.
                printResult.add("");
                printResult.add("----------[ " + element.id() + " ]----------");

                // Adds the full name on file.
                if (fullName != null) {
                    printResult.add("Name: " + fullName);
                }

                // Adds any alternative names on file.
                if (!alternativeNames.isEmpty()) {
                    printResult.add("Alternative Names: " + alternativeNames.toString()
                            .replace("[", "").replace("]", ""));
                }

                // Adds the age and birthday on file.
                if (age != null && birthday != null) {
                    printResult.add("Age: " + age + " (" + birthday + ")");
                }

                // Adds the email address on file.
                if (emailAddress != null) {
                    printResult.add("Email Address: " + emailAddress);
                }

                // Adds any phone numbers on file.
                if (!phoneNumbers.isEmpty()) {
                    phoneNumbers = phoneNumbers.stream().distinct().toList();

                    printResult.add("Phone Numbers: " + phoneNumbers.toString()
                            .replace("[", "").replace("]", ""));
                }

                // Adds any possible connections on file.
                if (!possibleConnections.isEmpty()) {
                    possibleConnections = possibleConnections.stream().distinct().toList();

                    printResult.add("Possible Connections: " + possibleConnections.toString()
                            .replace("[", "").replace("]", ""));
                }

                // Adds the address on file.
                if (address != null) {
                    printResult.add("Address: " + address);
                }

                // Adds the county on file.
                if (county != null) {
                    printResult.add("County: " + county);
                }

                // Adds the neighborhood on file.
                if (neighborhood != null) {
                    printResult.add("Neighborhood: " + neighborhood);
                }

                // Adds the home info on file.
                if (homeInfo != null) {
                    printResult.add("Home Info: " + homeInfo);
                }

                // Adds the bedroom/bathroom info on file.
                if (bedBath != null) {
                    printResult.add("Bed/Bath: " + bedBath);
                }

                // Adds the lot size on file.
                if (lotSize != null) {
                    printResult.add("Lot Size: " + lotSize);
                }

                // Adds the floor size on file.
                if (floorSize != null) {
                    printResult.add("Floor Size: " + floorSize);
                }

                // Adds the parcel id on file.
                if (parcelId != null) {
                    printResult.add("Parcel ID: " + parcelId);
                }

                // Adds the last sale info on file.
                if (lastSaleDate != null && lastSalePrice != null) {
                    printResult.add("Last Sale: " + lastSaleDate + " (" + lastSalePrice + ")");
                }

                // Adds the FIPS info on file.
                if (fips != null) {
                    printResult.add("FIPS: " + fips);
                }

                // Adds the latitude & longitude on file.
                if (latLong != null) {
                    printResult.add("Lat/Long: " + latLong);
                }

                // Adds any past addresses on file.
                if (!pastAddress.isEmpty()) {
                    pastAddress = pastAddress.stream().distinct().toList();

                    printResult.add("");
                    printResult.add("Past Addresses: ");

                    for (String line : pastAddress) {
                        printResult.add("- " + line);
                    }
                }

                // Adds the results print string list to the master list.
                printResults.add(printResult);
            }
        }

        if (resultsFound == 0) {
            System.out.println();
            System.out.println("No results found.");
            System.out.println("Try again with a different query.");

        } else {
            System.out.println();
            System.out.println("Results found: " + resultsFound);

            for (List<String> results : printResults) {
                results.forEach(System.out::println);
            }
        }

        System.exit(0);
    }

    public static String getWebsiteSrc(String website) {
        try {
            URL url = new URL(website);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
            con.setRequestProperty("content-type", "application/json; utf-8");
            con.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            con.setRequestProperty("accept-language", "en-US,en;q=0.9");
            con.setRequestProperty("upgrade-insecure-requests", "1");
            con.setRequestProperty("dnt", "1");
            con.setRequestProperty("cache-control", "max-age=0");
            con.setRequestProperty("sec-ch-ua", "\"Chromium\";v=\"106\", \"Google Chrome\";v=\"106\", \"Not;A=Brand\";v=\"99\"");
            con.setRequestProperty("sec-ch-ua-mobile", "?0");
            con.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
            con.setRequestProperty("sec-gpc", "1");
            con.setConnectTimeout(2000);
            con.setReadTimeout(2000);

            // Tests the connection before grabbing the source code.
            // If we're being blocked, return blocked and move on.
            try {
                con.getInputStream();
            } catch (IOException ignored) {
                return "Blocked";
            }

            // Grabs the website's source code and returns it in String form.
            InputStream input = con.getInputStream();
            String websiteSrc = getStringFromStream(input);
            con.disconnect();
            return websiteSrc;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "Not found";
    }

    public static String getStringFromStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;

        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException ex) {
            ex.printStackTrace();

        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return sb.toString();
    }
}
