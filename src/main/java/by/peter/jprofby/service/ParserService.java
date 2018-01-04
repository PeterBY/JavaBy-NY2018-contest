package by.peter.jprofby.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ParserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParserService.class);

    @Value("${start_url}")
    private String startUrl;

    private String protocol;

    @Value("${regex}")
    private String regex;

    @Value("${filter_end_with}")
    private String[] filterEndWith;

    @Value("${filter_contains}")
    private String[] filterContains;

    public int parseAndCount() throws IOException {
        int counter = 0;

        Set<String> allUrls = new HashSet<>();
        Deque<String> urlsDeque = new LinkedList<>();

        allUrls.add(startUrl);
        urlsDeque.add(startUrl);

        protocol = startUrl.substring(0, startUrl.indexOf("//"));

        while (!urlsDeque.isEmpty()) {
            String url = urlsDeque.pop();
            LOGGER.debug(url);
            try {
                Document page = getPage(url);
                if (page == null) {
                    continue;
                }

                counter += getCount(page, regex);

                Set<String> pageUrls = getUrls(page);
                pageUrls.removeAll(allUrls);
                allUrls.addAll(pageUrls);
                urlsDeque.addAll(pageUrls);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("{}: {}", e.getLocalizedMessage(), url);
            }
        }

        return counter;
    }

    private Document getPage(String url) {
        Document page = null;
        try {
            page = Jsoup.connect(url).get();
        } catch (IOException e) {
            LOGGER.error("{}: {}", e.getLocalizedMessage(), url);
        }
        return page;
    }

    private Set<String> getUrls(Document page) {
        Set<String> urls = new HashSet<>();
        Elements links = page.select("a[href]");
        for (Element element : links) {
            String url = element.attr("abs:href");

            if (!url.startsWith(protocol)) {
                int index = url.indexOf("//");
                if (index > 0) {
                    url = protocol + url.substring(index);
                }
            }

            if (!url.startsWith(protocol)) {
                int index = url.indexOf("//");
                if (index > 0) {
                    url = protocol + url.substring(index);
                } else {
                    continue;
                }
            }

            if (url.lastIndexOf('/') == url.length() - 1) {
                url = url.substring(0, url.length() - 1);
            }

            if (!urls.contains(url) && checkUrl(url)) {
                urls.add(url);
            }
        }
        return urls;
    }

    private int getCount(Document page, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(page.html());
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }

    private boolean checkUrl(String url) {
        if (!(url.substring(url.indexOf("//")).startsWith(startUrl.substring(url.indexOf("//"))))) {
            return false;
        }

        for (String s : filterEndWith) {
            if (url.endsWith(s)) {
                return false;
            }
        }

        for (String s : filterContains) {
            if (url.contains(s)) {
                return false;
            }
        }

        return true;
    }
}
