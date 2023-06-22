package it.polito.wa2.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.brands.Brand
import it.polito.wa2.server.brands.BrandRepository
import it.polito.wa2.server.categories.Category
import it.polito.wa2.server.categories.CategoryRepository
import it.polito.wa2.server.categories.ProductCategory
import it.polito.wa2.server.items.ItemDTO
import it.polito.wa2.server.items.ItemRepository
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.json.BasicJsonParser
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI
import java.sql.Timestamp
import java.util.*


@Testcontainers
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
class ItemControllerTests {
    val json = BasicJsonParser()
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:latest")

        @Container
        val keycloak = KeycloakContainer().withRealmImportFile("keycloak/realm-test.json")

        var managerToken = ""
        var clientToken = ""
        var expertToken = ""
        var vendorToken = ""


        @JvmStatic
        @BeforeAll
        fun setup(){
            keycloak.start()
            TestUtils.testKeycloakSetup(keycloak)
            managerToken = TestUtils.testKeycloakGetManagerToken(keycloak)
            clientToken = TestUtils.testKeycloakGetClientToken(keycloak)
            expertToken = TestUtils.testKeycloakGetExpertToken(keycloak)
            vendorToken = TestUtils.testKeycloakGetVendorToken(keycloak)
        }

        @JvmStatic
        @AfterAll
        fun clean(){
            keycloak.stop()
            postgres.close()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") {"create-drop"}
            registry.add("spring.datasource.hikari.validation-timeout"){"250"}
            registry.add("spring.datasource.hikari.connection-timeout"){"250"}
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri")
            { keycloak.authServerUrl + "realms/SpringBootKeycloak"}
        }
    }
    @LocalServerPort
    protected var port: Int = 8080
    @Autowired
    lateinit var restTemplate: TestRestTemplate
    @Autowired
    lateinit var productRepository: ProductRepository
    @Autowired
    lateinit var brandRepository:BrandRepository
    @Autowired
    lateinit var categoryRepository: CategoryRepository
    @Autowired
    lateinit var itemRepository: ItemRepository
    @Autowired
    lateinit var profileRepository: ProfileRepository


    @Test
    fun postItemSuccess() {
        val uri = URI("http://localhost:$port/API/vendor/products/items/")

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        productRepository.save(product1)

        val itemEntity = ItemDTO(
            "0000000000000",
            123451234,
            null,
            null,
            null,
            12
        )

        val entity = TestUtils.testEntityHeader(itemEntity, vendorToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val createdItem = itemRepository.findByProductAndSerialNum(product1, 123451234)

        Assertions.assertNotNull(createdItem)

        itemRepository.delete(createdItem!!)
        productRepository.delete(product1)
        categoryRepository.delete(category)
        brandRepository.delete(brand)
    }

    @Test
    fun getItemsSuccess() {

        val uri = URI("http://localhost:$port/API/authenticated/products/0000000000000/items/")

        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        profileRepository.save(customer)

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        productRepository.save(product1)

        val item = TestUtils.testItem(product1, customer, UUID.randomUUID(), 12341234, 12, Timestamp(System.currentTimeMillis()))
        itemRepository.save(item)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(1, body.size)

        itemRepository.delete(item)
        productRepository.delete(product1)
        categoryRepository.delete(category)
        brandRepository.delete(brand)
        profileRepository.delete(customer)
    }
    @Test
    fun getItemSuccess() {

        val uri = URI("http://localhost:$port/API/authenticated/products/0000000000000/items/12341234")

        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        profileRepository.save(customer)

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        productRepository.save(product1)

        val item = TestUtils.testItem(product1, customer, UUID.randomUUID(), 12341234, 12, Timestamp(System.currentTimeMillis()))
        itemRepository.save(item)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(product1.productId, body["productId"])
        Assertions.assertEquals(item.serialNum, body["serialNum"])

        itemRepository.delete(item)
        productRepository.delete(product1)
        categoryRepository.delete(category)
        brandRepository.delete(brand)
        profileRepository.delete(customer)
    }
    @Test
    fun getAllItems() {

        val uri = URI("http://localhost:$port/API/client/profiles/items")

        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        profileRepository.save(customer)

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        productRepository.save(product1)

        val item = TestUtils.testItem(product1, customer, UUID.randomUUID(), 12341234, 12, Timestamp(System.currentTimeMillis()))
        itemRepository.save(item)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(1, body.size)

        itemRepository.delete(item)
        productRepository.delete(product1)
        categoryRepository.delete(category)
        brandRepository.delete(brand)
        profileRepository.delete(customer)
    }
    @Test
    fun assignItemSuccess() {

        val uri = URI("http://localhost:$port/API/client/products/items/register")

        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        profileRepository.save(customer)

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        productRepository.save(product1)

        val item = TestUtils.testItem(product1, customer, UUID.randomUUID(), 12341234, 12, Timestamp(System.currentTimeMillis()))
        item.client=null
        itemRepository.save(item)

        val itemEntity = object {
            val productId = "0000000000000"
            val serialNum = "12341234"
            val clientEmail = "client@polito.it"
            val uuid = item.uuid
        }

        val entity = TestUtils.testEntityHeader(itemEntity, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.PUT,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val item2 = itemRepository.findByProductAndSerialNum(product1, 12341234)
        Assertions.assertEquals(item2!!.client!!.email, "client@polito.it")

        itemRepository.delete(item)
        productRepository.delete(product1)
        categoryRepository.delete(category)
        brandRepository.delete(brand)
        profileRepository.delete(customer)
    }
}

