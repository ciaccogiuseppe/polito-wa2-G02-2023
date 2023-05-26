package it.polito.wa2.server

import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.util.ProxyUtils
import java.io.Serializable

@MappedSuperclass
abstract class EntityBase<T: Serializable> {
    companion object {
        private const val serialVersionUID = -43869754L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id:T? = null

    fun getId():T? = id
    override fun toString():String{
        return "@Entity ${this.javaClass.name}(id=$id)"
    }

    override fun equals(other:Any?):Boolean{
        if(other == null) return false
        if(other === this) return true
        if(javaClass != ProxyUtils.getUserClass(other))
            return false
        other as EntityBase<*>
        return if (null == id) false
        else this.id == other.id
    }

    override fun hashCode(): Int{
        return 31
    }
}