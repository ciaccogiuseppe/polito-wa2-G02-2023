package it.polito.wa2.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.brands.Brand
import it.polito.wa2.server.brands.BrandRepository
import it.polito.wa2.server.categories.Category
import it.polito.wa2.server.categories.CategoryRepository
import it.polito.wa2.server.categories.ProductCategory
import it.polito.wa2.server.products.ProductDTO
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.products.toDTO
import it.polito.wa2.server.profiles.ProfileDTO
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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI


@Testcontainers
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
class ProductControllerTests {
    val json = BasicJsonParser()
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:latest")

        @Container
        val keycloak = KeycloakContainer().withRealmImportFile("keycloak/realm-test.json")

        var managerToken = ""
        var clientToken = ""
        var expertToken = ""


        @JvmStatic
        @BeforeAll
        fun setup(){
            keycloak.start()
            TestUtils.testKeycloakSetup(keycloak)
            managerToken = TestUtils.testKeycloakGetManagerToken(keycloak)
            clientToken = TestUtils.testKeycloakGetClientToken(keycloak)
            expertToken = TestUtils.testKeycloakGetExpertToken(keycloak)
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

    @Test
    //@DirtiesContext
    fun getExistingProductsUnauthorized() {
        val uri = URI("http://localhost:$port/API/public/products/")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)


        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", brand,category)
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", brand,category)
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)


        val entity = HttpEntity(null, null)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )


        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 3)

        Assertions.assertEquals(true, body.any{a -> a["productId"] == product1.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product2.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product3.productId})

        Assertions.assertEquals(true, body.any{a -> a["name"] == product1.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product2.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product3.name})

        Assertions.assertEquals(true, body.any{a -> a["brand"] == product1.brand!!.name})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product2.brand!!.name})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product3.brand!!.name})

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

        brandRepository.delete(brand)
        categoryRepository.delete(category)

    }
    @Test
    //@DirtiesContext
    fun getExistingProductsManager() {
        val uri = URI("http://localhost:$port/API/public/products/")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", brand,category)
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", brand,category)
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 3)

        Assertions.assertEquals(true, body.any{a -> a["productId"] == product1.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product2.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product3.productId})

        Assertions.assertEquals(true, body.any{a -> a["name"] == product1.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product2.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product3.name})

        Assertions.assertEquals(true, body.any{a -> a["brand"] == product1.brand!!.name})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product2.brand!!.name})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product3.brand!!.name})

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

        brandRepository.delete(brand)
        categoryRepository.delete(category)

    }

    @Test
    //@DirtiesContext
    fun getExistingProductsClient() {
        val uri = URI("http://localhost:$port/API/public/products/")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", brand,category)
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", brand,category)
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 3)

        Assertions.assertEquals(true, body.any{a -> a["productId"] == product1.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product2.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product3.productId})

        Assertions.assertEquals(true, body.any{a -> a["name"] == product1.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product2.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product3.name})

        Assertions.assertEquals(true, body.any{a -> a["brand"] == product1.brand!!.name})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product2.brand!!.name})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product3.brand!!.name})

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

        brandRepository.delete(brand)
        categoryRepository.delete(category)
    }

    @Test
    //@DirtiesContext
    fun getExistingProductsExpert() {
        val uri = URI("http://localhost:$port/API/public/products/")
        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", brand,category)
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", brand,category)
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 3)

        Assertions.assertEquals(true, body.any{a -> a["productId"] == product1.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product2.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product3.productId})

        Assertions.assertEquals(true, body.any{a -> a["name"] == product1.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product2.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product3.name})

        Assertions.assertEquals(true, body.any{a -> a["brand"] == product1.brand!!.name})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product2.brand!!.name})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product3.brand!!.name})

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

        brandRepository.delete(brand)
        categoryRepository.delete(category)

    }


    @Test
    //@DirtiesContext
    fun getExistingProductManager() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000000")
        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", brand,category)
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", brand,category)
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(product1.productId, body["productId"])
        Assertions.assertEquals(product1.name, body["name"])
        Assertions.assertEquals(product1.brand!!.name, body["brand"])

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

        brandRepository.delete(brand)
        categoryRepository.delete(category)
    }

    @Test
    //@DirtiesContext
    fun getExistingProductClient() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000000")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", brand,category)
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", brand,category)
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

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
        Assertions.assertEquals(product1.name, body["name"])
        Assertions.assertEquals(product1.brand!!.name, body["brand"])

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

        brandRepository.delete(brand)
        categoryRepository.delete(category)
    }

    @Test
    //@DirtiesContext
    fun getExistingProductExpert() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000000")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", brand,category)
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", brand,category)
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(product1.productId, body["productId"])
        Assertions.assertEquals(product1.name, body["name"])
        Assertions.assertEquals(product1.brand!!.name, body["brand"])

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)
        brandRepository.delete(brand)
        categoryRepository.delete(category)
    }

    @Test
    //@DirtiesContext
    fun getProductUnauthorized() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000000")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", brand,category)
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", brand,category)
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = HttpEntity(null, null)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(product1.productId, body["productId"])
        Assertions.assertEquals(product1.name, body["name"])
        Assertions.assertEquals(product1.brand!!.name, body["brand"])

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)
        brandRepository.delete(brand)
        categoryRepository.delete(category)
    }

    @Test
    //@DirtiesContext
    fun getNonExistingProductManager() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000003")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", brand,category)
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", brand,category)
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)
        brandRepository.delete(brand)
        categoryRepository.delete(category)
    }

    @Test
    //@DirtiesContext
    fun getNonExistingProductClient() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000003")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", brand,category)
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", brand,category)
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)
        brandRepository.delete(brand)
        categoryRepository.delete(category)
    }

    @Test
    //@DirtiesContext
    fun getNonExistingProductExpert() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000003")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", brand,category)
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", brand,category)
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)
        brandRepository.delete(brand)
        categoryRepository.delete(category)
    }

    @Test
    //@DirtiesContext
    fun getProductWrongIdManager() {
        val uri = URI("http://localhost:$port/API/public/products/000000000000")
        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)
        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand, category)
        productRepository.save(product1)

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        productRepository.delete(product1)
        brandRepository.delete(brand)
        categoryRepository.delete(category)
    }

    @Test
    //@DirtiesContext
    fun getProductWrongIdClient() {
        val uri = URI("http://localhost:$port/API/public/products/000000000000")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand, category)

        productRepository.save(product1)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        productRepository.delete(product1)
        brandRepository.delete(brand)
        categoryRepository.delete(category)
    }

    @Test
    //@DirtiesContext
    fun getProductWrongIdExpert() {
        val uri = URI("http://localhost:$port/API/public/products/000000000000")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)
        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand, category)
        productRepository.save(product1)

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        productRepository.delete(product1)
        brandRepository.delete(brand)
        categoryRepository.delete(category)
    }


    @Test
    fun postProductSuccess() {
        val uri = URI("http://localhost:$port/API/manager/products/")

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)

        val productEntity = object {
            val productId = "0000000000000"
            val name = "PC Omen Intel i7"
            val brand = brand.name
            val category = category.name.toString()
        }

        val entity = TestUtils.testEntityHeader(productEntity, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val createdProduct = productRepository.findByIdOrNull(productEntity.productId)

        Assertions.assertNotNull(createdProduct)
        Assertions.assertEquals(productEntity.name, createdProduct?.name)
        Assertions.assertEquals(brand, createdProduct?.brand)
        Assertions.assertEquals(category, createdProduct?.category)

        productRepository.delete(createdProduct!!)
        categoryRepository.delete(category)
        brandRepository.delete(brand)
    }
    @Test
    fun postProductNullProduct() {
        val uri = URI("http://localhost:$port/API/manager/products/")

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)

        val productEntity = null

        val entity = TestUtils.testEntityHeader(productEntity, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)

        categoryRepository.delete(category)
        brandRepository.delete(brand)
    }

    @Test
    fun postProductWrongIDFormat() {
        val uri = URI("http://localhost:$port/API/manager/products/")

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)



        val productEntity = object {
            val productId = "000000000000"
            val name = "PC Omen Intel i7"
            val brand = brand.name
            val category = category.name.toString()
        }

        val entity = TestUtils.testEntityHeader(productEntity, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        categoryRepository.delete(category)
        brandRepository.delete(brand)
    }

    @Test
    fun postProductWrongProductFormat() {
        val uri = URI("http://localhost:$port/API/manager/products/")

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)




        val productEntity = object {
            val productId = "0000000000000"
            val name = "PC Omen Intel i7"
            val category = category.name.toString()
            val brand = ""
        }

        val entity = TestUtils.testEntityHeader(productEntity, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        categoryRepository.delete(category)
        brandRepository.delete(brand)
    }

    @Test
    fun postProductNotExistingBrand() {
        val uri = URI("http://localhost:$port/API/manager/products/")

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)

        val productEntity = object {
            val productId = "0000000000000"
            val name = "PC Omen Intel i7"
            val brand = "abc"
            val category = category.name.toString()
        }



        val entity = TestUtils.testEntityHeader(productEntity, managerToken)


        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        categoryRepository.delete(category)
        brandRepository.delete(brand)
    }

    @Test
    fun postProductNotExistingCategory() {
        val uri = URI("http://localhost:$port/API/manager/products/")

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)


        val productEntity = object {
            val productId = "0000000000000"
            val name = "PC Omen Intel i7"
            val brand = brand.name
            val category = "abc"
        }

        val entity = TestUtils.testEntityHeader(productEntity, managerToken)


        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)

        categoryRepository.delete(category)
        brandRepository.delete(brand)
    }


    @Test
    fun postProductDuplicate() {
        val uri = URI("http://localhost:$port/API/manager/products/")

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name= ProductCategory.SMARTPHONE
        categoryRepository.save(category)


        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)
        productRepository.save(product1)



        val productEntity = object {
            val productId = "0000000000000"
            val name = "PC Omen Intel i7"
            val brand = brand.name
            val category = category.name.toString()
        }


        val entity = TestUtils.testEntityHeader(productEntity, managerToken)


        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.CONFLICT, result.statusCode)

        productRepository.delete(product1)
        categoryRepository.delete(category)
        brandRepository.delete(brand)

    }
}

