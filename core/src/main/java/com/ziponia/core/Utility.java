package com.ziponia.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 작성일 : 2018. 07. 08.
 * 작성자 Lee Ji Hoon <thtjwls@gmail.com>
 *
 * @project sns
 * <p>
 * 각종 유틸리티
 * <p>
 * ────────────────────────────────────────────────────────────────
 * 이력
 * ────────────────────────────────────────────────────────────────
 * 날짜           수정자           내용
 * ────────────────────────────────────────────────────────────────
 * 2018. 07. 08.  이지훈           Create
 */
public interface Utility extends Serializable {

    /**
     * 랜덤키를 만듭니다.
     *
     * @param length     key 길이를 정의합니다.
     * @param numberType true -> 숫자형식으로만 출력합니다. false -> 숫자 or 문자로 출력합니다.
     * @return java.lang.String random key
     */
    static String keyGenerator(int length, boolean numberType) {
        StringBuilder result = new StringBuilder();

        if (numberType) {
            for (int i = 0; i < length; i++) {
                double randomValue = Math.random();
                int intValue = (int) (randomValue * 10);
                result.append(intValue);
            }

        } else {
            String uid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, length);
            result.append(uid);
        }

        return result.toString();
    }

    /**
     * currentTimestamp 기준 이후의 분 시점을 반환합니다.
     *
     * @param currentTimestamp 변환 할 시간
     * @param afterMinute      변환 할 시점
     * @return java.sql.Timestamp
     */
    static Timestamp afterMinute(Timestamp currentTimestamp, int afterMinute) {
        int targetTime = 60 * afterMinute * 1000; // 초
        return new Timestamp(currentTimestamp.getTime() + targetTime);
    }

    /**
     * currentTimestamp 기준 이전의 분 시점을 반환합니다.
     *
     * @param currentTimestamp 변환 할 시간
     * @param beforeMinute     변환 할 시점
     * @return java.sql.Timestamp
     */
    static Timestamp beforeMinute(Timestamp currentTimestamp, int beforeMinute) {
        int targetTime = 60 * beforeMinute * 1000; // 초
        return new Timestamp(currentTimestamp.getTime() - targetTime);
    }

    /**
     * 문자열을 SHA-512 로 변환합니다.
     *
     * @param str target String
     * @return java.lang.String
     */
    static String encryptSHA512(String str) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte aHash : hash) {
                String hex = Integer.toHexString(0xff & aHash);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 바이너리 바이트 배열을 스트링으로 변환
     */
    static String byteArrayToBinaryString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; ++i) {
            sb.append(byteToBinaryString(b[i]));
        }
        return sb.toString();
    }

    /**
     * 바이너리 바이트를 스트링으로 변환
     */
    static String byteToBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
    }

    /**
     * 바이너리 스트링을 바이트배열로 변환
     */
    static byte[] binaryStringToByteArray(String s) {
        int count = s.length() / 8;
        byte[] b = new byte[count];
        for (int i = 1; i < count; ++i) {
            String t = s.substring((i - 1) * 8, i * 8);
            b[i - 1] = binaryStringToByte(t);
        }
        return b;
    }

    /**
     * 바이너리 스트링을 바이트로 변환
     */
    static byte binaryStringToByte(String s) {
        byte ret = 0, total = 0;
        for (int i = 0; i < 8; ++i) {
            ret = (s.charAt(7 - i) == '1') ? (byte) (1 << i) : 0;
            total = (byte) (ret | total);
        }
        return total;
    }

    /**
     * 운전 면허번호 유효성 체크
     * 지역명이 포함된 운전면허번호 조회시 경북-95-255933-61 방식으로 넘깁니다.
     * 숫자만 포함된 운전면허번호 조회시에도 19-95-255933-61 방식으로 통일합니다.
     *
     * @param licenseNo 유효성을 체크할 면허번호를 입력합니다.
     * @return boolean
     */
    static boolean checkDriveLicenseValidity(String licenseNo) {

        String regex = ".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*"; //한글이 포함되어 있는 운전면허번호 구분

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(licenseNo);

        if (matcher.matches()) {
            String regex1 = "^([가-힣]{2}(\\s|-)?|[가-힣]{2}-?)(\\s|-)?\\d{2}(\\s|-)?\\d{6}(\\s|-)?\\d{2}$"; //지역명 포함 운전면허번호
            pattern = Pattern.compile(regex1);
        } else {
            String regex2 = "^((1[1-9])|(2[0-6])|(26)|(28))(\\s|-)?\\d{2}(\\s|-)?\\d{6}(\\s|-)?\\d{2}$"; //숫자만 포함 운전면허번호
            pattern = Pattern.compile(regex2);
        }

        matcher = pattern.matcher(licenseNo);
        return matcher.matches();
    }

    /**
     * 위도, 경도를 사용하여 시작점과 끝점의 거리를 구합니다. (단위 km)
     *
     * @param startLat  시작위도
     * @param startLong 시작경도
     * @param endLat    목표위도
     * @param endLong   목표경도
     * @return Double
     * @see <a href="https://github.com/jasonwinn/haversine">참조링크</a>
     */
    static Double calculationDistanceFromGeographic(double startLat, double startLong, double endLat, double endLong) {

        // 지구 평균 반지름
        double EARTH_RADIUS = 6371;

        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(startLat) * Math.cos(endLat) * Math.pow(Math.sin(dLong / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 인자값으로, number 를 포맷팅 합니다.
     */
    static String formatNumber(int number) {
        String str = String.valueOf(number);
        String result = "";

        if (str.length() <= 3) {
            return str;
        }

        for (int i = 0; i < str.length(); i++) {
            char point = str.charAt(str.length() - 1 - i);
            if (i > 0 && i % 3 == 0) {
                result = point + "," + result;
                continue;
            }

            result = point + result;
        }

        return result;
    }

    /**
     * IP 허용 범위를 검사 (ipV6 는 기본적으로 검사하지 않습니다.)
     *
     * @param ip             검사 할 IP 주소 ip-v4
     * @param access_list_ip 허용범위를 체크 할 ip list
     * @return 허용되는 Ip 일경우 true 를 반환.
     * <p>
     * access_list_ip 에 각 호스트별 아이피 range 를 주어 체크 할 수 있습니다.
     * <p>
     * 예를들어,
     * <p>
     * access_list_ip = [0.0.0.0]; 일경우, 모든 아이피는 A Class 에서 매치 되므로, 액세스 됩니다.
     * access_list_ip = [192.3.0.0]; 일경우, 193.3.2.1 아이피는 B Class 에서 매치 되므로, 액세스 됩니다.
     */
    static boolean checkIpAddress(String ip, String[] access_list_ip) {
        boolean access = false;

        for (int i = 0; i < access_list_ip.length; i++) {
            String[] white_ip = access_list_ip[i].split("\\.");
            String[] input_ip = ip.split("\\.");

            // ip equals check
            if (access_list_ip[i].equals(ip)) {
                access = true;
                break;
            }

            if (input_ip.length == 4) {
                boolean range_check = false;

                for (int j = white_ip.length - 1; j >= 0; j--) {
                    if (white_ip[j].equals("0")) {
                        range_check = true;
                        continue;
                    }

                    if (!white_ip[j].equals(input_ip[j])) {
                        range_check = false;
                        break;
                    }
                }

                access = range_check;

                if (access) break;
            }
        }

        return access;
    }

    static void executionCommendLine(String... args) {

        Process proc = null;
        BufferedReader read = null;

        try {
            proc = Runtime.getRuntime().exec(args);
            read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            proc.waitFor();

            while (read.ready()) {
                System.out.println(read.readLine());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (read != null) {
                    read.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (proc != null) {
                proc.destroy();
            }
        }
    }
}
