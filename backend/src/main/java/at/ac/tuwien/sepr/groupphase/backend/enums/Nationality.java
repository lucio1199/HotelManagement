package at.ac.tuwien.sepr.groupphase.backend.enums;

import lombok.Getter;

/**
 * Enum that describes the nationality of a user.
 */
@Getter
public enum Nationality {
    AFG("Afghanistan"),
    ALB("Albania"),
    DZA("Algeria"),
    AND("Andorra"),
    AGO("Angola"),
    ATG("Antigua and Barbuda"),
    ARG("Argentina"),
    ARM("Armenia"),
    AUS("Australia"),
    AUT("Austria"),
    AZE("Azerbaijan"),
    BHS("Bahamas"),
    BHR("Bahrain"),
    BGD("Bangladesh"),
    BRB("Barbados"),
    BLR("Belarus"),
    BEL("Belgium"),
    BLZ("Belize"),
    BEN("Benin"),
    BTN("Bhutan"),
    BOL("Bolivia"),
    BIH("Bosnia and Herzegovina"),
    BWA("Botswana"),
    BRA("Brazil"),
    BRN("Brunei"),
    BGR("Bulgaria"),
    BFA("Burkina Faso"),
    BDI("Burundi"),
    CPV("Cabo Verde"),
    KHM("Cambodia"),
    CMR("Cameroon"),
    CAN("Canada"),
    CAF("Central African Republic"),
    TCD("Chad"),
    CHL("Chile"),
    CHN("China"),
    COL("Colombia"),
    COM("Comoros"),
    COD("Congo (Democratic Republic of the)"),
    COG("Congo"),
    CRI("Costa Rica"),
    CIV("Côte d'Ivoire"),
    HRV("Croatia"),
    CUB("Cuba"),
    CYP("Cyprus"),
    CZE("Czechia"),
    DNK("Denmark"),
    DJI("Djibouti"),
    DMA("Dominica"),
    DOM("Dominican Republic"),
    ECU("Ecuador"),
    EGY("Egypt"),
    SLV("El Salvador"),
    GNQ("Equatorial Guinea"),
    ERI("Eritrea"),
    EST("Estonia"),
    SWZ("Eswatini"),
    ETH("Ethiopia"),
    FJI("Fiji"),
    FIN("Finland"),
    FRA("France"),
    GAB("Gabon"),
    GMB("Gambia"),
    GEO("Georgia"),
    DEU("Germany"),
    GHA("Ghana"),
    GRC("Greece"),
    GRD("Grenada"),
    GTM("Guatemala"),
    GIN("Guinea"),
    GNB("Guinea-Bissau"),
    GUY("Guyana"),
    HTI("Haiti"),
    HND("Honduras"),
    HUN("Hungary"),
    ISL("Iceland"),
    IND("India"),
    IDN("Indonesia"),
    IRN("Iran"),
    IRQ("Iraq"),
    IRL("Ireland"),
    ISR("Israel"),
    ITA("Italy"),
    JAM("Jamaica"),
    JPN("Japan"),
    JOR("Jordan"),
    KAZ("Kazakhstan"),
    KEN("Kenya"),
    KIR("Kiribati"),
    PRK("Korea (North)"),
    KOR("Korea (South)"),
    KWT("Kuwait"),
    KGZ("Kyrgyzstan"),
    LAO("Laos"),
    LVA("Latvia"),
    LBN("Lebanon"),
    LSO("Lesotho"),
    LBR("Liberia"),
    LBY("Libya"),
    LIE("Liechtenstein"),
    LTU("Lithuania"),
    LUX("Luxembourg"),
    MDG("Madagascar"),
    MWI("Malawi"),
    MYS("Malaysia"),
    MDV("Maldives"),
    MLI("Mali"),
    MLT("Malta"),
    MHL("Marshall Islands"),
    MRT("Mauritania"),
    MUS("Mauritius"),
    MEX("Mexico"),
    FSM("Micronesia"),
    MDA("Moldova"),
    MCO("Monaco"),
    MNG("Mongolia"),
    MNE("Montenegro"),
    MAR("Morocco"),
    MOZ("Mozambique"),
    MMR("Myanmar"),
    NAM("Namibia"),
    NRU("Nauru"),
    NPL("Nepal"),
    NLD("Netherlands"),
    NZL("New Zealand"),
    NIC("Nicaragua"),
    NER("Niger"),
    NGA("Nigeria"),
    MKD("North Macedonia"),
    NOR("Norway"),
    OMN("Oman"),
    PAK("Pakistan"),
    PLW("Palau"),
    PAN("Panama"),
    PNG("Papua New Guinea"),
    PRY("Paraguay"),
    PER("Peru"),
    PHL("Philippines"),
    POL("Poland"),
    PRT("Portugal"),
    QAT("Qatar"),
    ROU("Romania"),
    RUS("Russia"),
    RWA("Rwanda"),
    KNA("Saint Kitts and Nevis"),
    LCA("Saint Lucia"),
    VCT("Saint Vincent and the Grenadines"),
    SMR("San Marino"),
    STP("Sao Tome and Principe"),
    SAU("Saudi Arabia"),
    SEN("Senegal"),
    SRB("Serbia"),
    SYC("Seychelles"),
    SLE("Sierra Leone"),
    SGP("Singapore"),
    SVK("Slovakia"),
    SVN("Slovenia"),
    SLB("Solomon Islands"),
    SOM("Somalia"),
    ZAF("South Africa"),
    SSD("South Sudan"),
    ESP("Spain"),
    LKA("Sri Lanka"),
    SDN("Sudan"),
    SUR("Suriname"),
    SWE("Sweden"),
    CHE("Switzerland"),
    SYR("Syria"),
    TWN("Taiwan"),
    TJK("Tajikistan"),
    TZA("Tanzania"),
    THA("Thailand"),
    TLS("Timor-Leste"),
    TGO("Togo"),
    TON("Tonga"),
    TTO("Trinidad and Tobago"),
    TUN("Tunisia"),
    TUR("Turkey"),
    TKM("Turkmenistan"),
    TUV("Tuvalu"),
    UGA("Uganda"),
    UKR("Ukraine"),
    ARE("United Arab Emirates"),
    GBR("United Kingdom"),
    USA("United States"),
    URY("Uruguay"),
    UZB("Uzbekistan"),
    VUT("Vanuatu"),
    VEN("Venezuela"),
    VNM("Vietnam"),
    YEM("Yemen"),
    ZMB("Zambia"),
    ZWE("Zimbabwe");

    private final String countryName;

    Nationality(String countryName) {
        this.countryName = countryName;
    }

}
