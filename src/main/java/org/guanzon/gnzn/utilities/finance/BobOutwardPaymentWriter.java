package org.guanzon.gnzn.utilities.finance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds and writes a BOB "Outward Payment File" following the
 * "Outward Payment File Format - Regular BOB" template supplied by the bank
 * (sheet: "Updated"):
 *
 * <pre>
 *   File Extension       : TXT
 *   Field Separation Type: Delimited
 *   Delimiter            : Tab
 * </pre>
 *
 * <p>There is no header or trailer record in this format - the file is just
 * one tab-delimited detail line per {@link BobPayment}, 28 fields wide, in
 * the order the sheet documents them. See {@link BobPayment} for the full
 * field-by-field spec and the caveats about the sheet's own inconsistencies
 * (Source Account length, the undocumented "Foreign Transfer" remittance
 * code, and the Beneficiary Currency Code type).
 */
public final class BobOutwardPaymentWriter {

    /** Column separator, per spec ("Delimiter: Tab"). */
    public static final String DELIMITER = "\t";

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private static final String CRLF = "\r\n";

    /**
     * Characters restricted in Beneficiary Address 1/2/3 per the sheet:
     * {@code *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|ÃƒÃ±ñÑ} - the trailing run
     * read here as mojibake for {@code ñÑ}. Reconfirm with the bank if a
     * legitimate address is ever rejected because of this list.
     */
    private static final String RESTRICTED_ADDRESS_CHARS = "*'~`:;%&!#$,@()^-_=+{}[]<>/.?\\|ñÑ";

    /** Max field lengths straight from the "Length" column of the sheet. */
    private static final class Len {
        static final int AMOUNT = 15;
        // Sheet disagrees with itself here: Format column says X(50), Length
        // column says 12. Using the more permissive 50 - see BobPayment javadoc.
        static final int SOURCE_ACCOUNT = 50;
        static final int FIRST_NAME = 255;
        static final int MIDDLE_NAME = 255;
        static final int BENEFICIARY_NAME = 255;
        static final int BENEFICIARY_ADDRESS = 35;
        static final int BENEFICIARY_ACCOUNT_NUMBER = 100;
        static final int BENEFICIARY_BANK = 255;
        static final int BENEFICIARY_BANK_ADDRESS = 255;
        static final int BENEFICIARY_INFORMATION = 255;
        static final int BANK_TO_BANK_INFORMATION = 255;
        static final int DATE_ESTABLISHED = 10;
        static final int PLACE_OF_INCORPORATION = 50;
        static final int NATIONALITY = 35;
        static final int NATURE_OF_BUSINESS = 35;
        static final int BENEFICIARY_CURRENCY_CODE = 3;
        static final int PURPOSE_CODE = 10;
        static final int NATURE_OF_TRANSFER = 255;
        static final int SWIFT_CODE = 15;
        static final int COUNTRY_OF_DESTINATION = 50;
        static final int IMPORTERS_CODE = 20;
        static final int ROUTING_NUMBER = 12;
        static final int RTGS_PURPOSE_CODE = 10;
    }

    /**
     * Validates every payment in the batch against the sheet's mandatory /
     * conditional-mandatory rules and its field-length caps. Throws
     * IllegalArgumentException describing the first problem found.
     */
    public void validate(BobPaymentBatch batch) {
        if (batch.getPayments().isEmpty()) {
            throw new IllegalArgumentException("At least one DETAIL (payment) record is required");
        }
        for (BobPayment p : batch.getPayments()) {
            validatePayment(p);
        }
    }

    private void validatePayment(BobPayment p) {
        String who = describeFor(p);

        requireMaxLength("Amount", formatAmount(p.getAmount()), Len.AMOUNT, who);
        if (p.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero (" + who + ")");
        }

        if (isBlank(p.getSourceAccount())) {
            throw new IllegalArgumentException("Source Account is MANDATORY (" + who + ")");
        }
        requireMaxLength("Source Account", p.getSourceAccount(), Len.SOURCE_ACCOUNT, who);

        requireMaxLength("First Name", p.getFirstName(), Len.FIRST_NAME, who);
        requireMaxLength("Middle Name", p.getMiddleName(), Len.MIDDLE_NAME, who);
        requireMaxLength("Beneficiary Name", p.getBeneficiaryName(), Len.BENEFICIARY_NAME, who);

        requireAddress("Beneficiary Address 1", p.getBeneficiaryAddress1(), who);
        requireAddress("Beneficiary Address 2", p.getBeneficiaryAddress2(), who);
        requireAddress("Beneficiary Address 3", p.getBeneficiaryAddress3(), who);

        if (isBlank(p.getBeneficiaryAccountNumber())) {
            throw new IllegalArgumentException("Beneficiary Account Number is MANDATORY (" + who + ")");
        }
        requireMaxLength("Beneficiary Account Number", p.getBeneficiaryAccountNumber(),
                Len.BENEFICIARY_ACCOUNT_NUMBER, who);

        if (isBlank(p.getBeneficiaryBank())) {
            throw new IllegalArgumentException("Beneficiary Bank is MANDATORY (" + who + ")");
        }
        requireMaxLength("Beneficiary Bank", p.getBeneficiaryBank(), Len.BENEFICIARY_BANK, who);

        if (isBlank(p.getBeneficiaryBankAddress())) {
            throw new IllegalArgumentException("Beneficiary Bank Address is MANDATORY (" + who + ")");
        }
        requireMaxLength("Beneficiary Bank Address", p.getBeneficiaryBankAddress(),
                Len.BENEFICIARY_BANK_ADDRESS, who);

        requireMaxLength("Beneficiary Information", p.getBeneficiaryInformation(),
                Len.BENEFICIARY_INFORMATION, who);

        if (p.getChargeType() == null) {
            throw new IllegalArgumentException("Charge Type is MANDATORY (" + who + ")");
        }

        requireMaxLength("Bank to Bank Information", p.getBankToBankInformation(),
                Len.BANK_TO_BANK_INFORMATION, who);

        if (p.getDateEstablished() == null) {
            throw new IllegalArgumentException("Date Established is MANDATORY (" + who + ")");
        }
        requireMaxLength("Date Established", formatDate(p.getDateEstablished()), Len.DATE_ESTABLISHED, who);

        if (isBlank(p.getPlaceOfIncorporation())) {
            throw new IllegalArgumentException("Place of Incorporation is MANDATORY (" + who + ")");
        }
        requireMaxLength("Place of Incorporation", p.getPlaceOfIncorporation(),
                Len.PLACE_OF_INCORPORATION, who);

        if (isBlank(p.getNationality())) {
            throw new IllegalArgumentException("Nationality is MANDATORY (" + who + ")");
        }
        requireMaxLength("Nationality", p.getNationality(), Len.NATIONALITY, who);

        if (isBlank(p.getNatureOfBusiness())) {
            throw new IllegalArgumentException("Nature of Business is MANDATORY (" + who + ")");
        }
        requireMaxLength("Nature of Business", p.getNatureOfBusiness(), Len.NATURE_OF_BUSINESS, who);

        if (!p.isCorporation() && isBlank(p.getFirstName())) {
            throw new IllegalArgumentException(
                    "First Name is MANDATORY when Is Corporation = 0 (" + who + ")");
        }

        if (isBlank(p.getBeneficiaryCurrencyCode())) {
            throw new IllegalArgumentException("Beneficiary Currency Code is MANDATORY (" + who + ")");
        }
        requireMaxLength("Beneficiary Currency Code", p.getBeneficiaryCurrencyCode(),
                Len.BENEFICIARY_CURRENCY_CODE, who);

        if (p.isForeignTransfer()) {
            if (isBlank(p.getPurposeCode())) {
                throw new IllegalArgumentException(
                        "Purpose Code is MANDATORY when Remittance Type = Foreign Transfer (" + who + ")");
            }
            requireMaxLength("Purpose Code", p.getPurposeCode(), Len.PURPOSE_CODE, who);

            if (isBlank(p.getNatureOfTransfer())) {
                throw new IllegalArgumentException(
                        "Nature of Transfer is MANDATORY when Remittance Type = Foreign Transfer (" + who + ")");
            }
            requireMaxLength("Nature of Transfer", p.getNatureOfTransfer(), Len.NATURE_OF_TRANSFER, who);

            if (isBlank(p.getSwiftCode())) {
                throw new IllegalArgumentException(
                        "Swift Code is MANDATORY when Remittance Type = Foreign Transfer (" + who + ")");
            }
            requireMaxLength("Swift Code", p.getSwiftCode(), Len.SWIFT_CODE, who);

            if (isBlank(p.getCountryOfDestination())) {
                throw new IllegalArgumentException(
                        "Country of Destination is MANDATORY when Remittance Type = Foreign Transfer (" + who + ")");
            }
            requireMaxLength("Country of Destination", p.getCountryOfDestination(),
                    Len.COUNTRY_OF_DESTINATION, who);

            if (isBlank(p.getImportersCode())) {
                throw new IllegalArgumentException(
                        "Importers Code is MANDATORY when Remittance Type = Foreign Transfer (" + who + ")");
            }
            requireMaxLength("Importers Code", p.getImportersCode(), Len.IMPORTERS_CODE, who);

            if (isBlank(p.getRoutingNumber())) {
                throw new IllegalArgumentException(
                        "Routing Number is MANDATORY when Remittance Type = Foreign Transfer (" + who + ")");
            }
            requireMaxLength("Routing Number", p.getRoutingNumber(), Len.ROUTING_NUMBER, who);
        }

        if (p.getRemittanceType() == BobPayment.RemittanceType.RTGS) {
            if (isBlank(p.getRtgsPurposeCode())) {
                throw new IllegalArgumentException(
                        "RTGS Purpose Code is MANDATORY when Remittance Type = RTGS (" + who + ")");
            }
            requireMaxLength("RTGS Purpose Code", p.getRtgsPurposeCode(), Len.RTGS_PURPOSE_CODE, who);
        }
    }

    /** Builds every DETAIL line, in the order the payments were added, without writing anything. */
    public List<String> buildLines(BobPaymentBatch batch) {
        validate(batch);
        List<String> lines = new ArrayList<String>();
        for (BobPayment p : batch.getPayments()) {
            lines.add(buildDetailLine(p));
        }
        return lines;
    }

    /** Validates, builds, and writes the file to disk (UTF-8, CRLF line endings). */
    public void writeToFile(BobPaymentBatch batch, File destination) throws IOException {
        List<String> lines = buildLines(batch);
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(destination.toPath()), StandardCharsets.UTF_8));
        try {
            for (String line : lines) {
                writer.write(line);
                writer.write(CRLF);
            }
        } finally {
            writer.close();
        }
    }

    // ------------------------------------------------------------- detail

    private String buildDetailLine(BobPayment p) {
        List<String> fields = new ArrayList<String>(28);
        fields.add(p.getRemittanceType().getCode());                        // 1
        fields.add(formatAmount(p.getAmount()));                            // 2
        fields.add(nullToEmpty(p.getSourceAccount()));                      // 3
        fields.add(nullToEmpty(p.getFirstName()));                          // 4
        fields.add(nullToEmpty(p.getMiddleName()));                         // 5
        fields.add(nullToEmpty(p.getBeneficiaryName()));                    // 6
        fields.add(nullToEmpty(p.getBeneficiaryAddress1()));                // 7
        fields.add(nullToEmpty(p.getBeneficiaryAddress2()));                // 8
        fields.add(nullToEmpty(p.getBeneficiaryAddress3()));                // 9
        fields.add(nullToEmpty(p.getBeneficiaryAccountNumber()));           // 10
        fields.add(nullToEmpty(p.getBeneficiaryBank()));                    // 11
        fields.add(nullToEmpty(p.getBeneficiaryBankAddress()));             // 12
        fields.add(nullToEmpty(p.getBeneficiaryInformation()));             // 13
        fields.add(p.getChargeType().getCode());                           // 14
        fields.add(nullToEmpty(p.getBankToBankInformation()));              // 15
        fields.add(formatDate(p.getDateEstablished()));                     // 16
        fields.add(nullToEmpty(p.getPlaceOfIncorporation()));               // 17
        fields.add(nullToEmpty(p.getNationality()));                        // 18
        fields.add(nullToEmpty(p.getNatureOfBusiness()));                   // 19
        fields.add(p.isCorporation() ? "1" : "0");                         // 20
        fields.add(nullToEmpty(p.getBeneficiaryCurrencyCode()));            // 21
        fields.add(nullToEmpty(p.getPurposeCode()));                        // 22
        fields.add(nullToEmpty(p.getNatureOfTransfer()));                   // 23
        fields.add(nullToEmpty(p.getSwiftCode()));                          // 24
        fields.add(nullToEmpty(p.getCountryOfDestination()));               // 25
        fields.add(nullToEmpty(p.getImportersCode()));                      // 26
        fields.add(nullToEmpty(p.getRoutingNumber()));                      // 27
        fields.add(nullToEmpty(p.getRtgsPurposeCode()));                    // 28
        return join(fields);
    }

    // ------------------------------------------------------------ helpers

    private String join(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sb.append(DELIMITER);
            }
            sb.append(fields.get(i));
        }
        return sb.toString();
    }

    private String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    /** Amount rendered as "9999999999.99" - always 2 decimals, no thousands separator. */
    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void requireMaxLength(String fieldName, String value, int maxLength, String who) {
        String v = nullToEmpty(value);
        if (v.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " exceeds max length of " + maxLength
                            + " (got " + v.length() + " chars: \"" + v + "\") (" + who + ")");
        }
    }

    private void requireAddress(String fieldName, String value, String who) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(fieldName + " is MANDATORY (" + who + ")");
        }
        requireMaxLength(fieldName, value, Len.BENEFICIARY_ADDRESS, who);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (RESTRICTED_ADDRESS_CHARS.indexOf(c) >= 0) {
                throw new IllegalArgumentException(
                        fieldName + " contains a restricted character '" + c + "' (" + who + ")");
            }
        }
    }

    private String describeFor(BobPayment p) {
        return "beneficiary: " + nullToEmpty(p.getBeneficiaryName());
    }
}
