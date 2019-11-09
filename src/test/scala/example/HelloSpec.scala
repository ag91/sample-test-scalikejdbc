package example

import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import org.scalatest.BeforeAndAfterAll
import org.scalatest.fixture.FlatSpec

class HelloSpec extends FlatSpec with BeforeAndAfterAll with AutoRollback {

  // override def db = NamedDB('anotherdb).toDB

  override def beforeAll() {
    val driverClass = Class.forName("org.h2.Driver")
    val connectionPool = ConnectionPool.singleton("jdbc:h2:mem:hello", "user", "pass")
    implicit val session = AutoSession
    sql"""
    create table members (
      id serial not null primary key,
      name varchar(64),
      created_at timestamp not null
    )
    """.execute.apply()
  }

  override def fixture(implicit session: DBSession) {
    sql"insert into members values (1, ${"Alice"}, current_timestamp)".update.apply()
    sql"insert into members values (2, ${"Bob"}, current_timestamp)".update.apply()
  }

  it should "create members from table" in { implicit session =>
    // setup
    import java.time._
    // define a case class
    case class Member(id: Long, name: Option[String], createdAt: String)
    // define an object that knows how to transform the DB format in the model
    object Member extends SQLSyntaxSupport[Member] {
      override val tableName = "members"
      def apply(rs: WrappedResultSet) = new Member(
        rs.long("id"), rs.stringOpt("name"), rs.string("created_at"))
    }
    val members: List[Member] = sql"select * from members".map(rs => Member(rs)).list.apply()

    assert(members.map((m:Member) => (m.id, m.name)) == List((1,Some("Alice")), (2,Some("Bob"))))
  }

  it should "create entities from table" in { implicit session =>
    val entities: List[Map[String, Any]] = sql"select id,name from members".map(_.toMap).list.apply()

    assert(entities.toString == "List(Map(ID -> 1, NAME -> Alice), Map(ID -> 2, NAME -> Bob))")
  }

  it should "update table" in { implicit session =>
    val entities0: List[Map[String, Any]] = sql"select id,name from members".map(_.toMap).list.apply()
    sql"insert into members values (77, ${"Norman"}, current_timestamp)".update.apply()
    val entities1: List[Map[String, Any]] = sql"select id,name from members".map(_.toMap).list.apply()

    assert(entities1.diff(entities0) == List(Map("ID" -> 77, "NAME" -> "Norman")))
  }

  it should "roll back after last test" in { implicit session =>
    val entities: List[Map[String, Any]] = sql"select id,name from members".map(_.toMap).list.apply()

    assert(entities.toString == "List(Map(ID -> 1, NAME -> Alice), Map(ID -> 2, NAME -> Bob))")
  }

}
