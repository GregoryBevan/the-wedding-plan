package me.elgregos.theweddingplan.api.guest

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import me.elgregos.theweddingplan.AbstractEndpointIntegrationTest
import me.elgregos.theweddingplan.api.guest.AddGuestRequestFixtures.charlieDavis
import me.elgregos.theweddingplan.api.guest.UpdateGuestRequestFixtures.johnDoeUpdated
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID
import kotlin.test.Test

class GuestEndpointIT : AbstractEndpointIntegrationTest() {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `should allow CORS preflight for backoffice origin`() {
        restTestClient.options().uri("/api/guests")
            .header(HttpHeaders.ORIGIN, "http://localhost:5173")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,x-xsrf-token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173")
            .expectHeader().valueMatches(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ".*(?i)x-xsrf-token.*")
    }

    @Test
    fun `should redirect unauthenticated guest creation to google login`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(charlieDavis)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/oauth2/authorization/google")
    }

    @Test
    fun `should redirect unauthenticated guest listing to google login`() {
        restTestClient.get().uri("/api/guests")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/oauth2/authorization/google")
    }

    @Test
    fun `should redirect unauthenticated guest detail fetch to google login`() {
        restTestClient.get().uri("/api/guests/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/oauth2/authorization/google")
    }

    @Test
    fun `should redirect unauthenticated guest update to google login`() {
        val csrf = csrfContext()

        restTestClient.put().uri("/api/guests/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(johnDoeUpdated)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/oauth2/authorization/google")
    }

    @Test
    fun `should redirect unauthenticated guest archive to google login`() {
        val csrf = csrfContext()

        restTestClient.delete().uri("/api/guests/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/oauth2/authorization/google")
    }

    @Test
    fun `should redirect unauthenticated guest restore to google login`() {
        val csrf = csrfContext()

        restTestClient.post().uri("/api/guests/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/restoration")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FOUND)
            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/oauth2/authorization/google")
    }

    @Test
    fun `should add a new guest`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val initialCount = guestCount()
        val uniqueGuest = AddGuestRequest(
            firstName = charlieDavis.firstName,
            lastName = charlieDavis.lastName,
            email = "${UUID.randomUUID()}-${charlieDavis.email}"
        )

        val createdGuest = restTestClient.post().uri("/api/guests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(uniqueGuest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(GuestResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected created guest in response body")

        val persistedGuest = persistedGuestById(createdGuest.id)
        val count = guestCount()

        assertThat(createdGuest.toCreationContract()).isEqualTo(uniqueGuest.toExpectedCreationContract())
        assertThat(count).isEqualTo(initialCount + 1)
        assertThat(persistedGuest).isEqualTo(createdGuest.toPersistedGuestRecord())
    }

    @Test
    fun `should list guests with pagination`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        restTestClient.get().uri("/api/guests?page=0&size=1")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].id").exists()
            .jsonPath("$.items[0].firstName").exists()
            .jsonPath("$.page").isEqualTo(0)
            .jsonPath("$.size").isEqualTo(1)
            .jsonPath("$.totalItems").exists()
            .jsonPath("$.totalPages").exists()
    }

    @Test
    fun `should get guest by id`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val uniqueGuest = AddGuestRequest(
            firstName = "Lookup-${UUID.randomUUID()}",
            lastName = "Guest",
            email = "lookup-${UUID.randomUUID()}@example.com"
        )
        val createdGuest = createGuest(csrf, uniqueGuest)

        restTestClient.get().uri("/api/guests/${createdGuest.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(createdGuest.id)
            .jsonPath("$.firstName").isEqualTo(uniqueGuest.firstName)
            .jsonPath("$.lastName").isEqualTo(uniqueGuest.lastName)
            .jsonPath("$.email").isEqualTo(uniqueGuest.email)
    }

    @Test
    fun `should return not found when guest id does not exist on get`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        restTestClient.get().uri("/api/guests/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a99")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should return bad request for malformed guest id on get`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        restTestClient.get().uri("/api/guests/not-a-uuid")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should update guest by id`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val guestToUpdate = AddGuestRequest(
            firstName = "Before",
            lastName = "Update",
            email = "before-${UUID.randomUUID()}@example.com"
        )
        val createdGuest = createGuest(csrf, guestToUpdate)
        val updateRequest = johnDoeUpdated.copy(version = createdGuest.version)

        val updatedGuest = restTestClient.put().uri("/api/guests/${createdGuest.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(updateRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(GuestResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected updated guest in response body")

        val persistedGuest = persistedGuestById(createdGuest.id)

        assertThat(updatedGuest.firstName).isEqualTo(updateRequest.firstName)
        assertThat(updatedGuest.lastName).isEqualTo(updateRequest.lastName)
        assertThat(updatedGuest.email).isEqualTo(updateRequest.email)
        assertThat(updatedGuest.version).isEqualTo(updateRequest.version + 1)
        assertThat(persistedGuest.firstName).isEqualTo(updateRequest.firstName)
        assertThat(persistedGuest.lastName).isEqualTo(updateRequest.lastName)
        assertThat(persistedGuest.email).isEqualTo(updateRequest.email)
        assertThat(persistedGuest.version).isEqualTo(createdGuest.version + 1)
    }

    @Test
    fun `should archive guest by id`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val guestToArchive = AddGuestRequest(
            firstName = "Before",
            lastName = "Archive",
            email = "archive-${UUID.randomUUID()}@example.com"
        )
        val createdGuest = createGuest(csrf, guestToArchive)

        restTestClient.delete().uri("/api/guests/${createdGuest.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(GuestResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected archived guest in response body")

        val persistedGuest = deletedGuestById(createdGuest.id)

        assertThat(persistedGuest.version).isEqualTo(createdGuest.version + 1)
        assertThat(persistedGuest.deletionDate).isNotNull()
    }

    @Test
    fun `should return not found when guest id does not exist on archive`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        restTestClient.delete().uri("/api/guests/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a99")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should return bad request for malformed guest id on archive`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        restTestClient.delete().uri("/api/guests/not-a-uuid")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should return not found when archiving an already archived guest`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val guestToArchive = AddGuestRequest(
            firstName = "Already",
            lastName = "Archived",
            email = "already-archived-${UUID.randomUUID()}@example.com"
        )
        val createdGuest = createGuest(csrf, guestToArchive)

        restTestClient.delete().uri("/api/guests/${createdGuest.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk

        restTestClient.delete().uri("/api/guests/${createdGuest.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound

        val persistedGuest = deletedGuestById(createdGuest.id)

        assertThat(persistedGuest.version).isEqualTo(createdGuest.version + 1)
    }

    @Test
    fun `should return conflict when version is stale on update`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val guestToUpdate = AddGuestRequest(
            firstName = "Before",
            lastName = "Update",
            email = "before-${UUID.randomUUID()}@example.com"
        )
        val createdGuest = createGuest(csrf, guestToUpdate)
        val staleUpdateRequest = johnDoeUpdated.copy(version = Long.MAX_VALUE)

        restTestClient.put().uri("/api/guests/${createdGuest.id}")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(staleUpdateRequest)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `should return not found when guest id does not exist on update`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val updateRequest = johnDoeUpdated.copy(version = 1L)

        restTestClient.put().uri("/api/guests/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a99")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(updateRequest)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should return bad request for malformed guest id on update`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val updateRequest = johnDoeUpdated.copy(version = 1L)

        restTestClient.put().uri("/api/guests/not-a-uuid")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(updateRequest)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should restore guest by id`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val guestToRestore = AddGuestRequest(
            firstName = "To",
            lastName = "Restore",
            email = "restore-${UUID.randomUUID()}@example.com"
        )
        val createdGuest = createGuest(csrf, guestToRestore)

        markGuestAsDeleted(createdGuest.id)

        val restoredGuest = restTestClient.post().uri("/api/guests/${createdGuest.id}/restoration")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody(GuestResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected restored guest in response body")

        val persistedGuest = persistedGuestById(createdGuest.id)

        assertThat(restoredGuest.id).isEqualTo(createdGuest.id)
        assertThat(restoredGuest.version).isEqualTo(createdGuest.version + 2)
        assertThat(persistedGuest.deletionDate).isEqualTo(null)
    }

    @Test
    fun `should return not found when guest id does not exist on restore`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")

        restTestClient.post().uri("/api/guests/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a99/restoration")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should return not found when restoring an active guest`() {
        val csrf = authenticatedCsrfContext("gregory@example.com")
        val activeGuest = AddGuestRequest(
            firstName = "Already",
            lastName = "Active",
            email = "active-${UUID.randomUUID()}@example.com"
        )
        val createdGuest = createGuest(csrf, activeGuest)

        restTestClient.post().uri("/api/guests/${createdGuest.id}/restoration")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound

        val persistedGuest = persistedGuestById(createdGuest.id)

        assertThat(persistedGuest.deletionDate).isEqualTo(null)
        assertThat(persistedGuest.version).isEqualTo(createdGuest.version)
    }

    private fun guestCount() =
        jdbcTemplate.queryForObject("select count(*) from guest where deletion_date is null", Int::class.java) ?: 0

    private fun createGuest(csrf: CsrfContext, request: AddGuestRequest): GuestResponse =
        restTestClient.post().uri("/api/guests")
            .header(HttpHeaders.COOKIE, csrf.cookies)
            .header("X-XSRF-TOKEN", csrf.csrfToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody(GuestResponse::class.java)
            .returnResult()
            .responseBody
            ?: error("Expected created guest in response body")

    private fun persistedGuestById(id: String): PersistedGuestRecord =
        jdbcTemplate.queryForObject(
            """
            select id, version, creation_date, update_date, deletion_date, first_name, last_name, email
            from guest
            where id = ?
            """.trimIndent(),
            { rs, _ ->
                PersistedGuestRecord(
                    id = rs.getObject("id", UUID::class.java).toString(),
                    version = rs.getLong("version"),
                    creationDate = rs.getTimestamp("creation_date").toLocalDateTime().toString(),
                    updateDate = rs.getTimestamp("update_date").toLocalDateTime().toString(),
                    deletionDate = rs.getTimestamp("deletion_date")?.toLocalDateTime()?.toString(),
                    firstName = rs.getString("first_name"),
                    lastName = rs.getString("last_name"),
                    email = rs.getString("email"),
                )
            },
            UUID.fromString(id)
        )

    private fun deletedGuestById(id: String): PersistedGuestRecord =
        jdbcTemplate.queryForObject(
            """
            select id, version, creation_date, update_date, deletion_date, first_name, last_name, email
            from guest
            where id = ? and deletion_date is not null
            """.trimIndent(),
            { rs, _ ->
                PersistedGuestRecord(
                    id = rs.getObject("id", UUID::class.java).toString(),
                    version = rs.getLong("version"),
                    creationDate = rs.getTimestamp("creation_date").toLocalDateTime().toString(),
                    updateDate = rs.getTimestamp("update_date").toLocalDateTime().toString(),
                    deletionDate = rs.getTimestamp("deletion_date")?.toLocalDateTime()?.toString(),
                    firstName = rs.getString("first_name"),
                    lastName = rs.getString("last_name"),
                    email = rs.getString("email"),
                )
            },
            UUID.fromString(id)
        )

    private fun GuestResponse.toPersistedGuestRecord() =
        PersistedGuestRecord(
            id = id,
            version = version,
            creationDate = creationDate,
            updateDate = updateDate,
            deletionDate = null,
            firstName = firstName,
            lastName = lastName,
            email = email,
        )

    private fun markGuestAsDeleted(id: String) {
        jdbcTemplate.update(
            """
            update guest
            set version = version + 1,
                update_date = now() at time zone 'utc',
                deletion_date = now() at time zone 'utc'
            where id = ?
            """.trimIndent(),
            UUID.fromString(id)
        )
    }

    private fun GuestResponse.toCreationContract() =
        GuestCreationContract(
            version = version,
            firstName = firstName,
            lastName = lastName,
            email = email,
        )

    private fun AddGuestRequest.toExpectedCreationContract() =
        GuestCreationContract(
            version = 1L,
            firstName = firstName,
            lastName = lastName,
            email = email,
        )

    private data class PersistedGuestRecord(
        val id: String,
        val version: Long,
        val creationDate: String,
        val updateDate: String,
        val deletionDate: String?,
        val firstName: String,
        val lastName: String,
        val email: String,
    )

    private data class GuestCreationContract(
        val version: Long,
        val firstName: String,
        val lastName: String,
        val email: String,
    )
}
