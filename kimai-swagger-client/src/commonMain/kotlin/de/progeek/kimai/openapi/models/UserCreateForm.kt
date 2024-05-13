/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package de.progeek.kimai.openapi.models


import kotlinx.serialization.*

/**
 * 
 *
 * @param username 
 * @param email 
 * @param language 
 * @param timezone 
 * @param plainPassword Plain text password
 * @param alias 
 * @param title 
 * @param accountNumber 
 * @param color The hexadecimal color code (default: #d2d6de)
 * @param roles 
 * @param plainApiToken Plain API token
 * @param enabled 
 * @param systemAccount 
 */
@Serializable

data class UserCreateForm (

    @SerialName(value = "username") @Required val username: kotlin.String,

    @SerialName(value = "email") @Required val email: kotlin.String,

    @SerialName(value = "language") @Required val language: UserCreateForm.Language,

    @SerialName(value = "timezone") @Required val timezone: kotlin.String,

    /* Plain text password */
    @SerialName(value = "plainPassword") @Required val plainPassword: kotlin.String,

    @SerialName(value = "alias") val alias: kotlin.String? = null,

    @SerialName(value = "title") val title: kotlin.String? = null,

    @SerialName(value = "accountNumber") val accountNumber: kotlin.String? = null,

    /* The hexadecimal color code (default: #d2d6de) */
    @SerialName(value = "color") val color: kotlin.String? = null,

    @SerialName(value = "roles") val roles: kotlin.collections.List<UserCreateForm.Roles>? = null,

    /* Plain API token */
    @SerialName(value = "plainApiToken") val plainApiToken: kotlin.String? = null,

    @SerialName(value = "enabled") val enabled: kotlin.Boolean? = null,

    @SerialName(value = "systemAccount") val systemAccount: kotlin.Boolean? = null

) {

    /**
     * 
     *
     * Values: ar,cs,csCZ,da,daDK,daGL,de,deAT,deBE,deCH,deDE,deIT,deLI,deLU,el,elCY,elGR,en,enAE,enAG,enAI,enAS,enAT,enAU,enBB,enBE,enBI,enBM,enBS,enBW,enBZ,enCA,enCC,enCH,enCK,enCM,enCX,enCY,enDE,enDK,enDM,enER,enFI,enFJ,enFK,enFM,enGB,enGD,enGG,enGH,enGI,enGM,enGU,enGY,enHK,enIE,enIL,enIM,enIN,enIO,enJE,enJM,enKE,enKI,enKN,enKY,enLC,enLR,enLS,enMG,enMH,enMO,enMP,enMS,enMT,enMU,enMV,enMW,enMY,enNA,enNF,enNG,enNL,enNR,enNU,enNZ,enPG,enPH,enPK,enPN,enPR,enPW,enRW,enSB,enSC,enSD,enSE,enSG,enSH,enSI,enSL,enSS,enSX,enSZ,enTC,enTK,enTO,enTT,enTV,enTZ,enUG,enUM,enUS,enVC,enVG,enVI,enVU,enWS,enZA,enZM,enZW,eo,es,esAR,esBO,esBR,esBZ,esCL,esCO,esCR,esCU,esDO,esEC,esES,esGQ,esGT,esHN,esMX,esNI,esPA,esPE,esPH,esPR,esPY,esSV,esUS,esUY,esVE,eu,euES,fa,faAF,faIR,fi,fiFI,fo,foDK,foFO,fr,frBE,frBF,frBI,frBJ,frBL,frCA,frCD,frCF,frCG,frCH,frCI,frCM,frDJ,frDZ,frFR,frGA,frGF,frGN,frGP,frGQ,frHT,frKM,frLU,frMA,frMC,frMF,frMG,frML,frMQ,frMR,frMU,frNC,frNE,frPF,frPM,frRE,frRW,frSC,frSN,frSY,frTD,frTG,frTN,frVU,frWF,frYT,he,heIL,hr,hrBA,hrHR,hu,huHU,`it`,itCH,itIT,itSM,itVA,ja,jaJP,ko,koKP,koKR,nbNO,nl,nlAW,nlBE,nlBQ,nlCW,nlNL,nlSR,nlSX,pl,plPL,pt,ptAO,ptBR,ptCH,ptCV,ptGQ,ptGW,ptLU,ptMO,ptMZ,ptPT,ptST,ptTL,ro,roMD,roRO,ru,ruBY,ruKG,ruKZ,ruMD,ruRU,ruUA,sk,skSK,sv,svAX,svFI,svSE,tr,trCY,trTR,uk,vi,viVN,zhCN,zhHant
     */
    @Serializable
    enum class Language(val value: kotlin.String) {
        @SerialName(value = "ar") ar("ar"),
        @SerialName(value = "cs") cs("cs"),
        @SerialName(value = "cs_CZ") csCZ("cs_CZ"),
        @SerialName(value = "da") da("da"),
        @SerialName(value = "da_DK") daDK("da_DK"),
        @SerialName(value = "da_GL") daGL("da_GL"),
        @SerialName(value = "de") de("de"),
        @SerialName(value = "de_AT") deAT("de_AT"),
        @SerialName(value = "de_BE") deBE("de_BE"),
        @SerialName(value = "de_CH") deCH("de_CH"),
        @SerialName(value = "de_DE") deDE("de_DE"),
        @SerialName(value = "de_IT") deIT("de_IT"),
        @SerialName(value = "de_LI") deLI("de_LI"),
        @SerialName(value = "de_LU") deLU("de_LU"),
        @SerialName(value = "el") el("el"),
        @SerialName(value = "el_CY") elCY("el_CY"),
        @SerialName(value = "el_GR") elGR("el_GR"),
        @SerialName(value = "en") en("en"),
        @SerialName(value = "en_AE") enAE("en_AE"),
        @SerialName(value = "en_AG") enAG("en_AG"),
        @SerialName(value = "en_AI") enAI("en_AI"),
        @SerialName(value = "en_AS") enAS("en_AS"),
        @SerialName(value = "en_AT") enAT("en_AT"),
        @SerialName(value = "en_AU") enAU("en_AU"),
        @SerialName(value = "en_BB") enBB("en_BB"),
        @SerialName(value = "en_BE") enBE("en_BE"),
        @SerialName(value = "en_BI") enBI("en_BI"),
        @SerialName(value = "en_BM") enBM("en_BM"),
        @SerialName(value = "en_BS") enBS("en_BS"),
        @SerialName(value = "en_BW") enBW("en_BW"),
        @SerialName(value = "en_BZ") enBZ("en_BZ"),
        @SerialName(value = "en_CA") enCA("en_CA"),
        @SerialName(value = "en_CC") enCC("en_CC"),
        @SerialName(value = "en_CH") enCH("en_CH"),
        @SerialName(value = "en_CK") enCK("en_CK"),
        @SerialName(value = "en_CM") enCM("en_CM"),
        @SerialName(value = "en_CX") enCX("en_CX"),
        @SerialName(value = "en_CY") enCY("en_CY"),
        @SerialName(value = "en_DE") enDE("en_DE"),
        @SerialName(value = "en_DK") enDK("en_DK"),
        @SerialName(value = "en_DM") enDM("en_DM"),
        @SerialName(value = "en_ER") enER("en_ER"),
        @SerialName(value = "en_FI") enFI("en_FI"),
        @SerialName(value = "en_FJ") enFJ("en_FJ"),
        @SerialName(value = "en_FK") enFK("en_FK"),
        @SerialName(value = "en_FM") enFM("en_FM"),
        @SerialName(value = "en_GB") enGB("en_GB"),
        @SerialName(value = "en_GD") enGD("en_GD"),
        @SerialName(value = "en_GG") enGG("en_GG"),
        @SerialName(value = "en_GH") enGH("en_GH"),
        @SerialName(value = "en_GI") enGI("en_GI"),
        @SerialName(value = "en_GM") enGM("en_GM"),
        @SerialName(value = "en_GU") enGU("en_GU"),
        @SerialName(value = "en_GY") enGY("en_GY"),
        @SerialName(value = "en_HK") enHK("en_HK"),
        @SerialName(value = "en_IE") enIE("en_IE"),
        @SerialName(value = "en_IL") enIL("en_IL"),
        @SerialName(value = "en_IM") enIM("en_IM"),
        @SerialName(value = "en_IN") enIN("en_IN"),
        @SerialName(value = "en_IO") enIO("en_IO"),
        @SerialName(value = "en_JE") enJE("en_JE"),
        @SerialName(value = "en_JM") enJM("en_JM"),
        @SerialName(value = "en_KE") enKE("en_KE"),
        @SerialName(value = "en_KI") enKI("en_KI"),
        @SerialName(value = "en_KN") enKN("en_KN"),
        @SerialName(value = "en_KY") enKY("en_KY"),
        @SerialName(value = "en_LC") enLC("en_LC"),
        @SerialName(value = "en_LR") enLR("en_LR"),
        @SerialName(value = "en_LS") enLS("en_LS"),
        @SerialName(value = "en_MG") enMG("en_MG"),
        @SerialName(value = "en_MH") enMH("en_MH"),
        @SerialName(value = "en_MO") enMO("en_MO"),
        @SerialName(value = "en_MP") enMP("en_MP"),
        @SerialName(value = "en_MS") enMS("en_MS"),
        @SerialName(value = "en_MT") enMT("en_MT"),
        @SerialName(value = "en_MU") enMU("en_MU"),
        @SerialName(value = "en_MV") enMV("en_MV"),
        @SerialName(value = "en_MW") enMW("en_MW"),
        @SerialName(value = "en_MY") enMY("en_MY"),
        @SerialName(value = "en_NA") enNA("en_NA"),
        @SerialName(value = "en_NF") enNF("en_NF"),
        @SerialName(value = "en_NG") enNG("en_NG"),
        @SerialName(value = "en_NL") enNL("en_NL"),
        @SerialName(value = "en_NR") enNR("en_NR"),
        @SerialName(value = "en_NU") enNU("en_NU"),
        @SerialName(value = "en_NZ") enNZ("en_NZ"),
        @SerialName(value = "en_PG") enPG("en_PG"),
        @SerialName(value = "en_PH") enPH("en_PH"),
        @SerialName(value = "en_PK") enPK("en_PK"),
        @SerialName(value = "en_PN") enPN("en_PN"),
        @SerialName(value = "en_PR") enPR("en_PR"),
        @SerialName(value = "en_PW") enPW("en_PW"),
        @SerialName(value = "en_RW") enRW("en_RW"),
        @SerialName(value = "en_SB") enSB("en_SB"),
        @SerialName(value = "en_SC") enSC("en_SC"),
        @SerialName(value = "en_SD") enSD("en_SD"),
        @SerialName(value = "en_SE") enSE("en_SE"),
        @SerialName(value = "en_SG") enSG("en_SG"),
        @SerialName(value = "en_SH") enSH("en_SH"),
        @SerialName(value = "en_SI") enSI("en_SI"),
        @SerialName(value = "en_SL") enSL("en_SL"),
        @SerialName(value = "en_SS") enSS("en_SS"),
        @SerialName(value = "en_SX") enSX("en_SX"),
        @SerialName(value = "en_SZ") enSZ("en_SZ"),
        @SerialName(value = "en_TC") enTC("en_TC"),
        @SerialName(value = "en_TK") enTK("en_TK"),
        @SerialName(value = "en_TO") enTO("en_TO"),
        @SerialName(value = "en_TT") enTT("en_TT"),
        @SerialName(value = "en_TV") enTV("en_TV"),
        @SerialName(value = "en_TZ") enTZ("en_TZ"),
        @SerialName(value = "en_UG") enUG("en_UG"),
        @SerialName(value = "en_UM") enUM("en_UM"),
        @SerialName(value = "en_US") enUS("en_US"),
        @SerialName(value = "en_VC") enVC("en_VC"),
        @SerialName(value = "en_VG") enVG("en_VG"),
        @SerialName(value = "en_VI") enVI("en_VI"),
        @SerialName(value = "en_VU") enVU("en_VU"),
        @SerialName(value = "en_WS") enWS("en_WS"),
        @SerialName(value = "en_ZA") enZA("en_ZA"),
        @SerialName(value = "en_ZM") enZM("en_ZM"),
        @SerialName(value = "en_ZW") enZW("en_ZW"),
        @SerialName(value = "eo") eo("eo"),
        @SerialName(value = "es") es("es"),
        @SerialName(value = "es_AR") esAR("es_AR"),
        @SerialName(value = "es_BO") esBO("es_BO"),
        @SerialName(value = "es_BR") esBR("es_BR"),
        @SerialName(value = "es_BZ") esBZ("es_BZ"),
        @SerialName(value = "es_CL") esCL("es_CL"),
        @SerialName(value = "es_CO") esCO("es_CO"),
        @SerialName(value = "es_CR") esCR("es_CR"),
        @SerialName(value = "es_CU") esCU("es_CU"),
        @SerialName(value = "es_DO") esDO("es_DO"),
        @SerialName(value = "es_EC") esEC("es_EC"),
        @SerialName(value = "es_ES") esES("es_ES"),
        @SerialName(value = "es_GQ") esGQ("es_GQ"),
        @SerialName(value = "es_GT") esGT("es_GT"),
        @SerialName(value = "es_HN") esHN("es_HN"),
        @SerialName(value = "es_MX") esMX("es_MX"),
        @SerialName(value = "es_NI") esNI("es_NI"),
        @SerialName(value = "es_PA") esPA("es_PA"),
        @SerialName(value = "es_PE") esPE("es_PE"),
        @SerialName(value = "es_PH") esPH("es_PH"),
        @SerialName(value = "es_PR") esPR("es_PR"),
        @SerialName(value = "es_PY") esPY("es_PY"),
        @SerialName(value = "es_SV") esSV("es_SV"),
        @SerialName(value = "es_US") esUS("es_US"),
        @SerialName(value = "es_UY") esUY("es_UY"),
        @SerialName(value = "es_VE") esVE("es_VE"),
        @SerialName(value = "eu") eu("eu"),
        @SerialName(value = "eu_ES") euES("eu_ES"),
        @SerialName(value = "fa") fa("fa"),
        @SerialName(value = "fa_AF") faAF("fa_AF"),
        @SerialName(value = "fa_IR") faIR("fa_IR"),
        @SerialName(value = "fi") fi("fi"),
        @SerialName(value = "fi_FI") fiFI("fi_FI"),
        @SerialName(value = "fo") fo("fo"),
        @SerialName(value = "fo_DK") foDK("fo_DK"),
        @SerialName(value = "fo_FO") foFO("fo_FO"),
        @SerialName(value = "fr") fr("fr"),
        @SerialName(value = "fr_BE") frBE("fr_BE"),
        @SerialName(value = "fr_BF") frBF("fr_BF"),
        @SerialName(value = "fr_BI") frBI("fr_BI"),
        @SerialName(value = "fr_BJ") frBJ("fr_BJ"),
        @SerialName(value = "fr_BL") frBL("fr_BL"),
        @SerialName(value = "fr_CA") frCA("fr_CA"),
        @SerialName(value = "fr_CD") frCD("fr_CD"),
        @SerialName(value = "fr_CF") frCF("fr_CF"),
        @SerialName(value = "fr_CG") frCG("fr_CG"),
        @SerialName(value = "fr_CH") frCH("fr_CH"),
        @SerialName(value = "fr_CI") frCI("fr_CI"),
        @SerialName(value = "fr_CM") frCM("fr_CM"),
        @SerialName(value = "fr_DJ") frDJ("fr_DJ"),
        @SerialName(value = "fr_DZ") frDZ("fr_DZ"),
        @SerialName(value = "fr_FR") frFR("fr_FR"),
        @SerialName(value = "fr_GA") frGA("fr_GA"),
        @SerialName(value = "fr_GF") frGF("fr_GF"),
        @SerialName(value = "fr_GN") frGN("fr_GN"),
        @SerialName(value = "fr_GP") frGP("fr_GP"),
        @SerialName(value = "fr_GQ") frGQ("fr_GQ"),
        @SerialName(value = "fr_HT") frHT("fr_HT"),
        @SerialName(value = "fr_KM") frKM("fr_KM"),
        @SerialName(value = "fr_LU") frLU("fr_LU"),
        @SerialName(value = "fr_MA") frMA("fr_MA"),
        @SerialName(value = "fr_MC") frMC("fr_MC"),
        @SerialName(value = "fr_MF") frMF("fr_MF"),
        @SerialName(value = "fr_MG") frMG("fr_MG"),
        @SerialName(value = "fr_ML") frML("fr_ML"),
        @SerialName(value = "fr_MQ") frMQ("fr_MQ"),
        @SerialName(value = "fr_MR") frMR("fr_MR"),
        @SerialName(value = "fr_MU") frMU("fr_MU"),
        @SerialName(value = "fr_NC") frNC("fr_NC"),
        @SerialName(value = "fr_NE") frNE("fr_NE"),
        @SerialName(value = "fr_PF") frPF("fr_PF"),
        @SerialName(value = "fr_PM") frPM("fr_PM"),
        @SerialName(value = "fr_RE") frRE("fr_RE"),
        @SerialName(value = "fr_RW") frRW("fr_RW"),
        @SerialName(value = "fr_SC") frSC("fr_SC"),
        @SerialName(value = "fr_SN") frSN("fr_SN"),
        @SerialName(value = "fr_SY") frSY("fr_SY"),
        @SerialName(value = "fr_TD") frTD("fr_TD"),
        @SerialName(value = "fr_TG") frTG("fr_TG"),
        @SerialName(value = "fr_TN") frTN("fr_TN"),
        @SerialName(value = "fr_VU") frVU("fr_VU"),
        @SerialName(value = "fr_WF") frWF("fr_WF"),
        @SerialName(value = "fr_YT") frYT("fr_YT"),
        @SerialName(value = "he") he("he"),
        @SerialName(value = "he_IL") heIL("he_IL"),
        @SerialName(value = "hr") hr("hr"),
        @SerialName(value = "hr_BA") hrBA("hr_BA"),
        @SerialName(value = "hr_HR") hrHR("hr_HR"),
        @SerialName(value = "hu") hu("hu"),
        @SerialName(value = "hu_HU") huHU("hu_HU"),
        @SerialName(value = "it") `it`("it"),
        @SerialName(value = "it_CH") itCH("it_CH"),
        @SerialName(value = "it_IT") itIT("it_IT"),
        @SerialName(value = "it_SM") itSM("it_SM"),
        @SerialName(value = "it_VA") itVA("it_VA"),
        @SerialName(value = "ja") ja("ja"),
        @SerialName(value = "ja_JP") jaJP("ja_JP"),
        @SerialName(value = "ko") ko("ko"),
        @SerialName(value = "ko_KP") koKP("ko_KP"),
        @SerialName(value = "ko_KR") koKR("ko_KR"),
        @SerialName(value = "nb_NO") nbNO("nb_NO"),
        @SerialName(value = "nl") nl("nl"),
        @SerialName(value = "nl_AW") nlAW("nl_AW"),
        @SerialName(value = "nl_BE") nlBE("nl_BE"),
        @SerialName(value = "nl_BQ") nlBQ("nl_BQ"),
        @SerialName(value = "nl_CW") nlCW("nl_CW"),
        @SerialName(value = "nl_NL") nlNL("nl_NL"),
        @SerialName(value = "nl_SR") nlSR("nl_SR"),
        @SerialName(value = "nl_SX") nlSX("nl_SX"),
        @SerialName(value = "pl") pl("pl"),
        @SerialName(value = "pl_PL") plPL("pl_PL"),
        @SerialName(value = "pt") pt("pt"),
        @SerialName(value = "pt_AO") ptAO("pt_AO"),
        @SerialName(value = "pt_BR") ptBR("pt_BR"),
        @SerialName(value = "pt_CH") ptCH("pt_CH"),
        @SerialName(value = "pt_CV") ptCV("pt_CV"),
        @SerialName(value = "pt_GQ") ptGQ("pt_GQ"),
        @SerialName(value = "pt_GW") ptGW("pt_GW"),
        @SerialName(value = "pt_LU") ptLU("pt_LU"),
        @SerialName(value = "pt_MO") ptMO("pt_MO"),
        @SerialName(value = "pt_MZ") ptMZ("pt_MZ"),
        @SerialName(value = "pt_PT") ptPT("pt_PT"),
        @SerialName(value = "pt_ST") ptST("pt_ST"),
        @SerialName(value = "pt_TL") ptTL("pt_TL"),
        @SerialName(value = "ro") ro("ro"),
        @SerialName(value = "ro_MD") roMD("ro_MD"),
        @SerialName(value = "ro_RO") roRO("ro_RO"),
        @SerialName(value = "ru") ru("ru"),
        @SerialName(value = "ru_BY") ruBY("ru_BY"),
        @SerialName(value = "ru_KG") ruKG("ru_KG"),
        @SerialName(value = "ru_KZ") ruKZ("ru_KZ"),
        @SerialName(value = "ru_MD") ruMD("ru_MD"),
        @SerialName(value = "ru_RU") ruRU("ru_RU"),
        @SerialName(value = "ru_UA") ruUA("ru_UA"),
        @SerialName(value = "sk") sk("sk"),
        @SerialName(value = "sk_SK") skSK("sk_SK"),
        @SerialName(value = "sv") sv("sv"),
        @SerialName(value = "sv_AX") svAX("sv_AX"),
        @SerialName(value = "sv_FI") svFI("sv_FI"),
        @SerialName(value = "sv_SE") svSE("sv_SE"),
        @SerialName(value = "tr") tr("tr"),
        @SerialName(value = "tr_CY") trCY("tr_CY"),
        @SerialName(value = "tr_TR") trTR("tr_TR"),
        @SerialName(value = "uk") uk("uk"),
        @SerialName(value = "vi") vi("vi"),
        @SerialName(value = "vi_VN") viVN("vi_VN"),
        @SerialName(value = "zh_CN") zhCN("zh_CN"),
        @SerialName(value = "zh_Hant") zhHant("zh_Hant");
    }
    /**
     * 
     *
     * Values: tEAMLEAD,aDMIN,sUPERADMIN
     */
    @Serializable
    enum class Roles(val value: kotlin.String) {
        @SerialName(value = "ROLE_TEAMLEAD") tEAMLEAD("ROLE_TEAMLEAD"),
        @SerialName(value = "ROLE_ADMIN") aDMIN("ROLE_ADMIN"),
        @SerialName(value = "ROLE_SUPER_ADMIN") sUPERADMIN("ROLE_SUPER_ADMIN");
    }
}

