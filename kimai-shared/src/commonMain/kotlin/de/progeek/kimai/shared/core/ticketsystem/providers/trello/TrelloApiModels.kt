package de.progeek.kimai.shared.core.ticketsystem.providers.trello

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Internal API response models for the Trello REST API v1.
 */

@Serializable
internal data class TrelloMemberResponse(
    val id: String,
    val username: String,
    val fullName: String? = null,
    val email: String? = null
)

@Serializable
internal data class TrelloBoardResponse(
    val id: String,
    val name: String,
    val desc: String? = null
)

@Serializable
internal data class TrelloListResponse(
    val id: String,
    val name: String
)

@Serializable
internal data class TrelloLabel(
    val name: String? = null,
    val color: String? = null
)

@Serializable
internal data class TrelloCardResponse(
    val id: String,
    val idShort: Int,
    val name: String,
    @SerialName("shortUrl")
    val shortUrl: String? = null,
    val idBoard: String,
    val idList: String,
    val dateLastActivity: String? = null,
    val labels: List<TrelloLabel> = emptyList(),
    val idMembers: List<String> = emptyList(),
    // Included when requested with list=true / card_list=true
    val list: TrelloListResponse? = null,
    // Included when requested with card_board=true (search endpoint)
    val board: TrelloBoardResponse? = null
)

@Serializable
internal data class TrelloSearchResponse(
    val cards: List<TrelloCardResponse> = emptyList()
)
