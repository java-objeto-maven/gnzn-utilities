package org.guanzon.gnzn.utilities.finance;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Demonstrates {@link BobOutwardPaymentWriter} with a Philippine-setting
 * sample batch: an individual beneficiary paid via PESONET, a corporation
 * paid via PESONET with ON-US charging, and an individual beneficiary paid
 * via RTGS (which requires an RTGS Purpose Code).
 *
 * <p>This mirrors {@code ExportPNB} (the PNB Corporate Check exporter) in
 * shape - a Builder-based model, a Writer with validate/buildLines/
 * writeToFile, and this demo class - but follows the BOB
 * "Outward Payment File Format - Regular" layout instead (28-field
 * tab-delimited detail lines, no header/trailer). It also supersedes the
 * quick-and-dirty {@code ExportBDO} in this same package for that same
 * underlying file family: same 28 fields, same delimiter, but built through
 * {@link BobPayment.Builder} with validation instead of raw
 * {@code String[][]} rows.
 *
 * Run with:
 *   javac -d out $(find src -name "*.java")
 *   java -cp out org.guanzon.gnzn.utilities.finance.ExportBOB
 */
public final class ExportBOB {

    private static final String SOURCE_ACCOUNT = "000000000001"; // sample funding/source account

    public static void main(String[] args) throws IOException {
        BobOutwardPaymentWriter writer = new BobOutwardPaymentWriter();
        BobPaymentBatch batch = buildSampleBatch();

        File outputFile = new File("BOB_Outward_Payment_20260920.txt");
        writer.writeToFile(batch, outputFile);
        System.out.println("Wrote " + outputFile.getAbsolutePath());
        for (String line : writer.buildLines(batch)) {
            System.out.println("  " + line);
        }
    }

    private static BobPaymentBatch buildSampleBatch() {
        BobPaymentBatch batch = new BobPaymentBatch();

        // 1) Individual beneficiary, domestic transfer via PESONET, beneficiary shoulders the charge.
        batch.addPayment(BobPayment.builder()
                .pesonet()
                .sourceAccount(SOURCE_ACCOUNT)
                .amount(new BigDecimal("15000.00"))
                .individual()
                .firstName("Juan")
                .middleName("Santos")
                .beneficiaryName("Dela Cruz")
                .beneficiaryAddress1("123 Rizal Street")
                .beneficiaryAddress2("Barangay Poblacion")
                .beneficiaryAddress3("Dagupan City")
                .beneficiaryAccountNumber("012000000001")
                .beneficiaryBank("BDO")
                .beneficiaryBankAddress("SM City Dagupan, Dagupan City")
                .chargeType(BobPayment.ChargeType.BENEFICIARY)
                .dateEstablished(LocalDate.of(1990, 4, 12))
                .placeOfIncorporation("Dagupan City")
                .nationality("Filipino")
                .natureOfBusiness("Employee")
                .beneficiaryCurrencyCode("PHP")
                .build());

        // 2) Corporation beneficiary, domestic transfer via PESONET, on-us charging.
        batch.addPayment(BobPayment.builder()
                .pesonet()
                .sourceAccount(SOURCE_ACCOUNT)
                .amount(new BigDecimal("48250.75"))
                .corporation()
                .beneficiaryName("ABC Trading Corporation")
                .beneficiaryAddress1("456 Quezon Avenue")
                .beneficiaryAddress2("Barangay San Vicente")
                .beneficiaryAddress3("Dagupan City")
                .beneficiaryAccountNumber("012000000002")
                .beneficiaryBank("BPI")
                .beneficiaryBankAddress("BPI Dagupan Branch, Dagupan City")
                .chargeType(BobPayment.ChargeType.ON_US)
                .dateEstablished(LocalDate.of(2005, 9, 1))
                .placeOfIncorporation("Pangasinan")
                .nationality("Filipino")
                .natureOfBusiness("Trading")
                .beneficiaryCurrencyCode("PHP")
                .build());

        // 3) Individual beneficiary, high-value transfer via RTGS (requires an RTGS Purpose Code).
        batch.addPayment(BobPayment.builder()
                .rtgs()
                .sourceAccount(SOURCE_ACCOUNT)
                .amount(new BigDecimal("1250000.00"))
                .individual()
                .firstName("Michael")
                .middleName("Torres")
                .beneficiaryName("Cuison")
                .beneficiaryAddress1("789 Burgos Street")
                .beneficiaryAddress2("Barangay Bonuan")
                .beneficiaryAddress3("Dagupan City")
                .beneficiaryAccountNumber("012000000077")
                .beneficiaryBank("BDO")
                .beneficiaryBankAddress("SM City Dagupan, Dagupan City")
                .chargeType(BobPayment.ChargeType.BENEFICIARY)
                .dateEstablished(LocalDate.of(1985, 1, 20))
                .placeOfIncorporation("Dagupan City")
                .nationality("Filipino")
                .natureOfBusiness("Business Owner")
                .beneficiaryCurrencyCode("PHP")
                .rtgsPurposeCode("SALARY")
                .build());

        return batch;
    }
}
