package com.souzip.global.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpRequestUtils {

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_X_REAL_IP = "X-Real-IP";
    private static final String HEADER_PROXY_CLIENT_IP = "Proxy-Client-IP";
    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String HEADER_X_APP_VERSION = "X-App-Version";

    private static final String UNKNOWN = "Unknown";
    private static final String UNKNOWN_IP = "unknown";

    private static final Pattern APP_VERSION_PATTERN =
        Pattern.compile("Souzip-(iOS|Android)/(\\d+\\.\\d+\\.\\d+)");

    public static String extractClientIp(HttpServletRequest request) {
        String ip = getFirstValidIp(request);
        return extractFirstIpFromCommaList(ip);
    }

    public static String extractUserAgent(HttpServletRequest request) {
        return getHeaderOrDefault(request, HEADER_USER_AGENT, UNKNOWN);
    }

    public static String extractAppVersion(HttpServletRequest request) {
        String version = request.getHeader(HEADER_X_APP_VERSION);

        if (isValidString(version)) {
            return version;
        }

        return parseVersionFromUserAgent(request);
    }

    private static String getFirstValidIp(HttpServletRequest request) {
        return Arrays.stream(new String[]{
                request.getHeader(HEADER_X_FORWARDED_FOR),
                request.getHeader(HEADER_X_REAL_IP),
                request.getHeader(HEADER_PROXY_CLIENT_IP),
                request.getRemoteAddr()
            })
            .filter(HttpRequestUtils::isValidIp)
            .findFirst()
            .orElse(UNKNOWN);
    }

    private static String parseVersionFromUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader(HEADER_USER_AGENT);

        if (isInvalidString(userAgent)) {
            return UNKNOWN;
        }

        return extractVersionFromMatcher(userAgent);
    }

    private static String extractVersionFromMatcher(String userAgent) {
        Matcher matcher = APP_VERSION_PATTERN.matcher(userAgent);

        if (hasMatchedVersion(matcher)) {
            return getVersionGroup(matcher);
        }

        return UNKNOWN;
    }

    private static boolean hasMatchedVersion(Matcher matcher) {
        return matcher.find();
    }

    private static String getVersionGroup(Matcher matcher) {
        return matcher.group(2);
    }

    private static String extractFirstIpFromCommaList(String ip) {
        if (isInvalidString(ip)) {
            return UNKNOWN;
        }

        if (containsComma(ip)) {
            return getFirstIpFromList(ip);
        }

        return ip;
    }

    private static boolean containsComma(String ip) {
        return ip.contains(",");
    }

    private static String getFirstIpFromList(String ipList) {
        String[] ips = splitByComma(ipList);
        return trimFirstIp(ips);
    }

    private static String[] splitByComma(String ipList) {
        return ipList.split(",");
    }

    private static String trimFirstIp(String[] ips) {
        return ips[0].trim();
    }

    private static String getHeaderOrDefault(HttpServletRequest request,
                                             String headerName,
                                             String defaultValue) {
        String value = request.getHeader(headerName);

        if (isValidString(value)) {
            return value;
        }

        return defaultValue;
    }

    private static boolean isValidIp(String ip) {
        if (isInvalidString(ip)) {
            return false;
        }

        if (isUnknownIp(ip)) {
            return false;
        }

        return true;
    }

    private static boolean isUnknownIp(String ip) {
        return equalsIgnoreCaseWithUnknown(ip);
    }

    private static boolean equalsIgnoreCaseWithUnknown(String ip) {
        return UNKNOWN_IP.equalsIgnoreCase(ip);
    }

    private static boolean isValidString(String str) {
        if (isNull(str)) {
            return false;
        }

        if (isEmptyAfterTrim(str)) {
            return false;
        }

        return true;
    }

    private static boolean isInvalidString(String str) {
        if (isNull(str)) {
            return true;
        }

        if (isEmptyAfterTrim(str)) {
            return true;
        }

        return false;
    }

    private static boolean isNull(String str) {
        return str == null;
    }

    private static boolean isEmptyAfterTrim(String str) {
        return trimString(str).isEmpty();
    }

    private static String trimString(String str) {
        return str.trim();
    }
}
