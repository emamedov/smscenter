package org.eminmamedov.smscenter.common;

import ie.omk.smpp.util.AlphabetEncoding;
import ie.omk.smpp.util.DefaultAlphabetEncoding;
import ie.omk.smpp.util.Latin1Encoding;
import ie.omk.smpp.util.MessageEncoding;
import ie.omk.smpp.util.UTF16Encoding;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Main SMSCenter utility class.
 * 
 * @author Emin Mamedov
 * 
 */
@Component
public class SmsCenterUtils {

    private static final int NUMBER_LENGTH = 11;

    @Value("${max.latin.sms.length}")
    private int maxLatinSmsLength;
    @Value("${min.latin.sms.length}")
    private int minLatinSmsLength;
    @Value("${max.utf.sms.length}")
    private int maxUtfSmsLength;
    @Value("${min.utf.sms.length}")
    private int minUtfSmsLength;

    /**
     * Converts input string to well-formated phone number. Right format for
     * phone numbers is 79XXXXXXXXX. Method removes from specified string all
     * symbols except of digits and guarantees that phone number will start with
     * 79.
     * 
     * @param numberOld
     *            phone number that should be formated
     * @return well-formated phone number in format 79XXXXXXXXX
     */
    public String checkNumber(String numberOld) {
        String newNumber = numberOld == null ? "" : numberOld.replaceAll("[^0-9]", "");
        if (newNumber.startsWith("89")) {
            newNumber = newNumber.replaceFirst("89", "79");
        }
        if (newNumber.startsWith("9")) {
            newNumber = "7" + newNumber;
        }
        return newNumber.length() == NUMBER_LENGTH && newNumber.startsWith("79") ? newNumber : null;
    }

    /**
     * Method splits incoming text depending on text encoding and text length.
     * 
     * @param text
     *            incoming text
     * @return text split depending on text encoding and text length
     */
    public List<String> splitMessage(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        boolean isUtf = false;
        try {
            isUtf = getTextEncoding(text) instanceof UTF16Encoding;
        } catch (UnsupportedEncodingException e) {
            return Collections.emptyList();
        }
        int minSmsLength = isUtf ? this.minUtfSmsLength : this.minLatinSmsLength;
        int maxSmsLength = isUtf ? this.maxUtfSmsLength : this.maxLatinSmsLength;
        List<String> parts = new ArrayList<String>();
        if (text.length() <= maxSmsLength) {
            parts.add(text);
        } else {
            parts = Arrays.asList(text.split("(?<=\\G.{" + minSmsLength + "})"));
        }
        return parts;
    }

    public MessageEncoding getTextEncoding(String text) throws UnsupportedEncodingException {
        if (text == null) {
            return null;
        }
        AlphabetEncoding[] supportedEncodings = { new Latin1Encoding(), new DefaultAlphabetEncoding() };
        for (AlphabetEncoding encoding : supportedEncodings) {
            if (text.equals(encoding.decodeString(encoding.encodeString(text)))) {
                return encoding;
            }
        }
        return new UTF16Encoding(true);
    }

}
