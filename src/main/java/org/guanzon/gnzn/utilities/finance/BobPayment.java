package org.guanzon.gnzn.utilities.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One DETAIL line from BOB's "Outward Payment File Format - Regular" template
 * (sheet: "Updated"): a single outward transfer instruction (RTGS or PESONET).
 *
 * <p>Unlike the PNB CC file layout, this template has no header/trailer record
 * at all - the whole file is just one tab-delimited detail line per payment,
 * position 1-28 exactly as documented in the sheet:
 *
 * <pre>
 *  1  Type of Remittance          Numeric  X(1)    0 = RTGS ; 2 = PESONET           Mandatory
 *  2  Amount                      Money    9999999999.99 (no comma separator)       Mandatory
 *  3  Source Account              Alnum    X(50)                                    Mandatory
 *  4  First Name                  Alnum    X(255)  Mandatory if "Is Corporation = 0" Mandatory
 *  5  Middle Name                 Alnum    X(255)                                    Optional
 *  6  Beneficiary Name            Alnum    X(255)  Last Name (individual) /          Mandatory
 *                                                    Corporation Name (corporation)
 *  7  Beneficiary Address 1       Alnum    X(35)   restricted characters apply       Mandatory
 *  8  Beneficiary Address 2       Alnum    X(35)   restricted characters apply       Mandatory
 *  9  Beneficiary Address 3       Alnum    X(35)   restricted characters apply       Mandatory
 * 10  Beneficiary Account Number  Alnum    X(100)                                    Mandatory
 * 11  Beneficiary Bank            Alnum    X(255)  code in Other Bank Maintenance    Mandatory
 * 12  Beneficiary Bank Address    Alnum    X(255)                                    Mandatory
 * 13  Beneficiary Information     Alnum    X(255)                                    Optional
 * 14  Charge Type                 Numeric  X(1)    0 = Beneficiary ; 1 = ON-US       Mandatory
 * 15  Bank to Bank Information    Alnum    X(255)                                    Optional
 * 16  Date Established            Date     MM/DD/YYYY                                Mandatory
 * 17  Place of Incorporation      Alnum    X(50)                                     Mandatory
 * 18  Nationality                 Alnum    X(35)   from Country maintenance          Mandatory
 * 19  Nature of Business          Alnum    X(35)                                     Mandatory
 * 20  Is Corporation               Numeric  X(1)    0 = Individual ; 1 = Corporation  Mandatory
 * 21  Beneficiary Currency Code   Numeric  X(3)                                      Mandatory
 * 22  Purpose Code                Alnum    X(10)   from Purpose Code Maintenance      Mandatory if Foreign Transfer
 * 23  Nature of Transfer          Alnum    X(255)                                    Mandatory if Foreign Transfer
 * 24  Swift Code                  Alnum    X(15)   from Other Bank Maintenance        Mandatory if Foreign Transfer
 * 25  Country of Destination      Alnum    X(50)   from Country Maintenance           Mandatory if Foreign Transfer
 * 26  Importers Code              Alnum    X(20)                                      Mandatory if Foreign Transfer
 * 27  Routing Number              Alnum    X(12)                                      Mandatory if Foreign Transfer
 * 28  RTGS Purpose Code           Alnum    X(10)   to be provided separately          Mandatory if RTGS
 * </pre>
 *
 * <p><b>Known gaps / caveats in the source sheet</b>, carried over here rather
 * than silently "fixed" (verify with the bank before relying on any of these):
 * <ul>
 *   <li>Field 1's documented values are only "0 = RTGS" and "2 = PESONET", yet
 *       fields 22-27 are conditioned on "Remittance Type = Foreign Transfer" -
 *       a value this sheet never assigns a code to. Since the file is named
 *       "... Regular BOB", foreign-transfer support is treated here as an
 *       opt-in extra: set {@link Builder#foreignTransfer()} to require fields
 *       22-27, but {@link #getRemittanceType()} only ever reports RTGS or
 *       PESONET until the bank documents an actual "Foreign Transfer" code.</li>
 *   <li>Field 3 (Source Account) is documented as both {@code X(50)} (Format
 *       column) and length {@code 12} (Length column). This code validates
 *       against 50 (the more permissive of the two) so real account numbers
 *       aren't rejected; confirm the true cap with the bank.</li>
 *   <li>Field 21 (Beneficiary Currency Code) is documented "Numeric X(3)", but
 *       this codebase's existing BDO exporter (same file family) populates it
 *       with the 3-letter alpha code {@code "PHP"} in practice. This class
 *       therefore keeps it a free-form string (max 3 chars) rather than
 *       enforcing digits-only.</li>
 *   <li>Beneficiary Address 1/2/3 "restricted special characters" are listed
 *       in the sheet as {@code *'~`:;%&!#$,@,(,)^-_=+{}[]<>/.?\|ÃƒÃ±ñÑ} - the
 *       trailing run is almost certainly mojibake for {@code ñÑ}. This is
 *       enforced as: {@code * ' ~ ` : ; % & ! # $ , @ ( ) ^ - _ = + { } [ ] < > / . ? \ | ñ Ñ}.
 *       Reconfirm with the bank if you ever see a legitimate address
 *       rejected for one of these.</li>
 * </ul>
 *
 * Built via {@link Builder} so the "mandatory if individual / corporation" and
 * "mandatory if RTGS / foreign transfer" rules can be checked in one place -
 * see {@link BobOutwardPaymentWriter#validate}.
 */
public final class BobPayment {

    /** Field 1 - Type of Remittance. Only the two codes the sheet documents. */
    public enum RemittanceType {
        RTGS("0"),
        PESONET("2");

        private final String code;

        RemittanceType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    /** Field 14 - Charge Type. */
    public enum ChargeType {
        BENEFICIARY("0"),
        ON_US("1");

        private final String code;

        ChargeType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    private final RemittanceType remittanceType;
    private final BigDecimal amount;
    private final String sourceAccount;
    private final String firstName;
    private final String middleName;
    private final String beneficiaryName;
    private final String beneficiaryAddress1;
    private final String beneficiaryAddress2;
    private final String beneficiaryAddress3;
    private final String beneficiaryAccountNumber;
    private final String beneficiaryBank;
    private final String beneficiaryBankAddress;
    private final String beneficiaryInformation;
    private final ChargeType chargeType;
    private final String bankToBankInformation;
    private final LocalDate dateEstablished;
    private final String placeOfIncorporation;
    private final String nationality;
    private final String natureOfBusiness;
    private final boolean corporation;
    private final String beneficiaryCurrencyCode;
    private final String purposeCode;
    private final String natureOfTransfer;
    private final String swiftCode;
    private final String countryOfDestination;
    private final String importersCode;
    private final String routingNumber;
    private final String rtgsPurposeCode;
    private final boolean foreignTransfer;

    private BobPayment(Builder b) {
        this.remittanceType = b.remittanceType;
        this.amount = b.amount;
        this.sourceAccount = b.sourceAccount;
        this.firstName = b.firstName;
        this.middleName = b.middleName;
        this.beneficiaryName = b.beneficiaryName;
        this.beneficiaryAddress1 = b.beneficiaryAddress1;
        this.beneficiaryAddress2 = b.beneficiaryAddress2;
        this.beneficiaryAddress3 = b.beneficiaryAddress3;
        this.beneficiaryAccountNumber = b.beneficiaryAccountNumber;
        this.beneficiaryBank = b.beneficiaryBank;
        this.beneficiaryBankAddress = b.beneficiaryBankAddress;
        this.beneficiaryInformation = b.beneficiaryInformation;
        this.chargeType = b.chargeType;
        this.bankToBankInformation = b.bankToBankInformation;
        this.dateEstablished = b.dateEstablished;
        this.placeOfIncorporation = b.placeOfIncorporation;
        this.nationality = b.nationality;
        this.natureOfBusiness = b.natureOfBusiness;
        this.corporation = b.corporation;
        this.beneficiaryCurrencyCode = b.beneficiaryCurrencyCode;
        this.purposeCode = b.purposeCode;
        this.natureOfTransfer = b.natureOfTransfer;
        this.swiftCode = b.swiftCode;
        this.countryOfDestination = b.countryOfDestination;
        this.importersCode = b.importersCode;
        this.routingNumber = b.routingNumber;
        this.rtgsPurposeCode = b.rtgsPurposeCode;
        this.foreignTransfer = b.foreignTransfer;
    }

    public RemittanceType getRemittanceType() {
        return remittanceType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public String getBeneficiaryAddress1() {
        return beneficiaryAddress1;
    }

    public String getBeneficiaryAddress2() {
        return beneficiaryAddress2;
    }

    public String getBeneficiaryAddress3() {
        return beneficiaryAddress3;
    }

    public String getBeneficiaryAccountNumber() {
        return beneficiaryAccountNumber;
    }

    public String getBeneficiaryBank() {
        return beneficiaryBank;
    }

    public String getBeneficiaryBankAddress() {
        return beneficiaryBankAddress;
    }

    public String getBeneficiaryInformation() {
        return beneficiaryInformation;
    }

    public ChargeType getChargeType() {
        return chargeType;
    }

    public String getBankToBankInformation() {
        return bankToBankInformation;
    }

    public LocalDate getDateEstablished() {
        return dateEstablished;
    }

    public String getPlaceOfIncorporation() {
        return placeOfIncorporation;
    }

    public String getNationality() {
        return nationality;
    }

    public String getNatureOfBusiness() {
        return natureOfBusiness;
    }

    public boolean isCorporation() {
        return corporation;
    }

    public String getBeneficiaryCurrencyCode() {
        return beneficiaryCurrencyCode;
    }

    public String getPurposeCode() {
        return purposeCode;
    }

    public String getNatureOfTransfer() {
        return natureOfTransfer;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public String getCountryOfDestination() {
        return countryOfDestination;
    }

    public String getImportersCode() {
        return importersCode;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public String getRtgsPurposeCode() {
        return rtgsPurposeCode;
    }

    /**
     * True if this payment was explicitly flagged as a foreign transfer via
     * {@link Builder#foreignTransfer()}, which makes fields 22-27 mandatory.
     * See the class javadoc caveat about field 1 never actually carrying a
     * documented "Foreign Transfer" code in this sheet.
     */
    public boolean isForeignTransfer() {
        return foreignTransfer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private RemittanceType remittanceType;
        private BigDecimal amount;
        private String sourceAccount;
        private String firstName = "";
        private String middleName = "";
        private String beneficiaryName;
        private String beneficiaryAddress1;
        private String beneficiaryAddress2;
        private String beneficiaryAddress3;
        private String beneficiaryAccountNumber;
        private String beneficiaryBank;
        private String beneficiaryBankAddress;
        private String beneficiaryInformation = "";
        private ChargeType chargeType = ChargeType.BENEFICIARY;
        private String bankToBankInformation = "";
        private LocalDate dateEstablished;
        private String placeOfIncorporation;
        private String nationality;
        private String natureOfBusiness;
        private boolean corporation = false;
        private String beneficiaryCurrencyCode;
        private String purposeCode = "";
        private String natureOfTransfer = "";
        private String swiftCode = "";
        private String countryOfDestination = "";
        private String importersCode = "";
        private String routingNumber = "";
        private String rtgsPurposeCode = "";
        private boolean foreignTransfer = false;

        private Builder() {
        }

        public Builder remittanceType(RemittanceType remittanceType) {
            this.remittanceType = remittanceType;
            return this;
        }

        public Builder rtgs() {
            this.remittanceType = RemittanceType.RTGS;
            return this;
        }

        public Builder pesonet() {
            this.remittanceType = RemittanceType.PESONET;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder amount(String amount) {
            this.amount = new BigDecimal(amount);
            return this;
        }

        public Builder sourceAccount(String sourceAccount) {
            this.sourceAccount = sourceAccount;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder middleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public Builder beneficiaryName(String beneficiaryName) {
            this.beneficiaryName = beneficiaryName;
            return this;
        }

        public Builder beneficiaryAddress1(String beneficiaryAddress1) {
            this.beneficiaryAddress1 = beneficiaryAddress1;
            return this;
        }

        public Builder beneficiaryAddress2(String beneficiaryAddress2) {
            this.beneficiaryAddress2 = beneficiaryAddress2;
            return this;
        }

        public Builder beneficiaryAddress3(String beneficiaryAddress3) {
            this.beneficiaryAddress3 = beneficiaryAddress3;
            return this;
        }

        public Builder beneficiaryAccountNumber(String beneficiaryAccountNumber) {
            this.beneficiaryAccountNumber = beneficiaryAccountNumber;
            return this;
        }

        public Builder beneficiaryBank(String beneficiaryBank) {
            this.beneficiaryBank = beneficiaryBank;
            return this;
        }

        public Builder beneficiaryBankAddress(String beneficiaryBankAddress) {
            this.beneficiaryBankAddress = beneficiaryBankAddress;
            return this;
        }

        public Builder beneficiaryInformation(String beneficiaryInformation) {
            this.beneficiaryInformation = beneficiaryInformation;
            return this;
        }

        public Builder chargeType(ChargeType chargeType) {
            this.chargeType = chargeType;
            return this;
        }

        public Builder bankToBankInformation(String bankToBankInformation) {
            this.bankToBankInformation = bankToBankInformation;
            return this;
        }

        public Builder dateEstablished(LocalDate dateEstablished) {
            this.dateEstablished = dateEstablished;
            return this;
        }

        public Builder placeOfIncorporation(String placeOfIncorporation) {
            this.placeOfIncorporation = placeOfIncorporation;
            return this;
        }

        public Builder nationality(String nationality) {
            this.nationality = nationality;
            return this;
        }

        public Builder natureOfBusiness(String natureOfBusiness) {
            this.natureOfBusiness = natureOfBusiness;
            return this;
        }

        public Builder corporation(boolean corporation) {
            this.corporation = corporation;
            return this;
        }

        /** Convenience for {@code corporation(true)}. */
        public Builder corporation() {
            this.corporation = true;
            return this;
        }

        /** Convenience for {@code corporation(false)}. */
        public Builder individual() {
            this.corporation = false;
            return this;
        }

        public Builder beneficiaryCurrencyCode(String beneficiaryCurrencyCode) {
            this.beneficiaryCurrencyCode = beneficiaryCurrencyCode;
            return this;
        }

        public Builder purposeCode(String purposeCode) {
            this.purposeCode = purposeCode;
            return this;
        }

        public Builder natureOfTransfer(String natureOfTransfer) {
            this.natureOfTransfer = natureOfTransfer;
            return this;
        }

        public Builder swiftCode(String swiftCode) {
            this.swiftCode = swiftCode;
            return this;
        }

        public Builder countryOfDestination(String countryOfDestination) {
            this.countryOfDestination = countryOfDestination;
            return this;
        }

        public Builder importersCode(String importersCode) {
            this.importersCode = importersCode;
            return this;
        }

        public Builder routingNumber(String routingNumber) {
            this.routingNumber = routingNumber;
            return this;
        }

        public Builder rtgsPurposeCode(String rtgsPurposeCode) {
            this.rtgsPurposeCode = rtgsPurposeCode;
            return this;
        }

        /**
         * Marks this payment as a foreign transfer, making fields 22-27
         * mandatory per the sheet. See the class javadoc caveat: this sheet
         * never actually assigns field 1 a code for "Foreign Transfer".
         */
        public Builder foreignTransfer() {
            this.foreignTransfer = true;
            return this;
        }

        public BobPayment build() {
            if (remittanceType == null) {
                throw new IllegalStateException("Type of Remittance is MANDATORY");
            }
            if (amount == null) {
                throw new IllegalStateException("Amount is MANDATORY");
            }
            if (beneficiaryName == null || beneficiaryName.trim().isEmpty()) {
                throw new IllegalStateException("Beneficiary Name is MANDATORY");
            }
            if (!corporation && (firstName == null || firstName.trim().isEmpty())) {
                throw new IllegalStateException(
                        "First Name is MANDATORY when Is Corporation = 0 (individual)");
            }
            if (dateEstablished == null) {
                throw new IllegalStateException("Date Established is MANDATORY");
            }
            return new BobPayment(this);
        }
    }
}
