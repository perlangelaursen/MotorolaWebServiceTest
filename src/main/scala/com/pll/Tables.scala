import slick.driver.H2Driver.api._
import slick.lifted.{ProvenShape, ForeignKeyQuery}

class Radios(tag : Tag) extends Table[(Int, String, Option[String])](tag, "RADIOS") {
  def id : Rep[Int] = column[Int]("RADIOID", O.PrimaryKey)
  def alias : Rep[String] = column[String]("RADIOALIAS")
  def location : Rep[Option[String]] = column[Option[String]]("RADIOLOCATION")

  def * : ProvenShape[(Int, String, Option[String])] = (id, alias, location)
}

class Locations(tag : Tag) extends Table[(Int, String)](tag, "LOCATIONS") {
  def id : Rep[Int] = column[Int]("RADIOID", O.PrimaryKey)
  def location : Rep[String] = column[String]("LOCATION", O.PrimaryKey)

  def * : ProvenShape[(Int, String)] = (id, location)
  def radio : ForeignKeyQuery[Radios, (Int, String, Option[String])] =
    foreignKey("ID_FK", id, TableQuery[Radios])(_.id)
}
