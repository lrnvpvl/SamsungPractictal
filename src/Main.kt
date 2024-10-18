import kotlin.random.Random

interface Character {
    val name: String
    var health: Int
    val maxHealth: Int
    fun attack(target: Character)
    fun heal()
    fun isAlive(): Boolean
    fun receiveDamage(damage: Int)
}

abstract class BaseCharacter(override val name: String, override var health: Int, override val maxHealth: Int) : Character {
    abstract fun chooseAction(): String
    abstract fun specialAction()
    open fun printUniqueStats() {}
    override fun isAlive(): Boolean = health > 0

    override fun receiveDamage(damage: Int) {
        health -= damage
        println("║ $name получает $damage урона. Осталось здоровья: $health")
        if (health <= 0) {
            println("║ $name погиб!")
        }
    }

    fun printStatus() {
        println("╔════════════════════════════════════════════════╗")
        println("║ $name (Здоровье: $health/$maxHealth) ║")
        printUniqueStats()
        println("╚════════════════════════════════════════════════╝")
    }
}

class Knight(name: String, health: Int, private val attackPower: Int) : BaseCharacter(name, health, health) {
    private var shieldHealth = health / 2
    private var shieldActive = false

    override fun attack(target: Character) {
        val damage = attackPower + Random.nextInt(1, 5)
        println("║ $name атакует ${target.name} и наносит $damage урона!")
        target.receiveDamage(damage)
    }

    override fun heal() {
        health = (health + 10).coerceAtMost(maxHealth)
        println("║ $name восстанавливает 10 здоровья. Текущее здоровье: $health")
    }

    override fun chooseAction(): String {
        print("Выберите действие для $name: 1 - Атака, 2 - Лечение, 3 - Защита, 4 - Бросок щита: ")
        return readln()
    }

    override fun specialAction() {
        if (shieldHealth > 0) {
            val damage = shieldHealth / 2
            println("║ $name бросает щит и наносит $damage урона!")
            val target = chooseTargetForSpecial()
            target.receiveDamage(damage)
            shieldHealth = 0
        } else {
            println("║ У $name нет щита для броска!")
        }
    }

    fun defend() {
        shieldActive = true
        println("║ $name активирует щит!")
    }

    override fun receiveDamage(damage: Int) {
        if (shieldActive && shieldHealth > 0) {
            val absorbed = (damage * 0.3).toInt()
            shieldHealth -= absorbed
            val remainingDamage = damage - absorbed
            println("║ Щит поглощает $absorbed урона. Осталось здоровья щита: $shieldHealth")
            super.receiveDamage(remainingDamage)
            if (shieldHealth <= 0) {
                println("║ Щит разрушен!")
                shieldActive = false
            }
        } else {
            super.receiveDamage(damage)
        }
    }

    override fun printUniqueStats() {
        println("║ Щит: $shieldHealth")
    }

    private fun chooseTargetForSpecial(): Character {
        val enemies = Game.characters.filter { it.isAlive() && it != this }
        return enemies[Random.nextInt(enemies.size)]
    }
}

class Mage(name: String, health: Int, private val attackPower: Int) : BaseCharacter(name, health, health) {
    private var mana = 40
    private val maxMana = 40

    override fun attack(target: Character) {
        print("Выберите силу удара для $name: 1 - Слабый удар (10 маны), 2 - Сильный удар (20 маны): ")
        val choice = readln().toInt()
        val damage = when (choice) {
            1 -> if (mana >= 10) {
                mana -= 10
                attackPower + Random.nextInt(1, 5)
            } else {
                println("Недостаточно маны для слабого удара!")
                return
            }
            2 -> if (mana >= 20) {
                mana -= 20
                attackPower + Random.nextInt(5, 15)
            } else {
                println("Недостаточно маны для сильного удара!")
                return
            }
            else -> {
                println("Неверный выбор!")
                return
            }
        }
        println("║ $name атакует ${target.name} и наносит $damage урона!")
        target.receiveDamage(damage)
    }

    override fun heal() {
        if (mana >= 35) {
            mana -= 35
            health = (health + 20).coerceAtMost(maxHealth)
            println("║ $name восстанавливает 20 здоровья. Текущее здоровье: $health")
        } else {
            println("║ Недостаточно маны для восстановления здоровья!")
        }
    }

    override fun chooseAction(): String {
        print("Выберите действие для $name: 1 - Атака, 2 - Лечение, 3 - Восстановить ману: ")
        return readln()
    }

    override fun specialAction() {
        mana = maxMana
        println("║ $name восстанавливает ману до максимума!")
    }

    fun passiveManaRegen() {
        mana = (mana + 5).coerceAtMost(maxMana)
        println("║ $name восстанавливает 5 маны. Текущая мана: $mana")
    }

    override fun printUniqueStats() {
        println("║ Мана: $mana/$maxMana")
    }
}

class Robot(name: String, health: Int, private val attackPower: Int) : BaseCharacter(name, health, health) {
    private var battery = 40
    private val maxBattery = 40
    private var inSleepMode = false

    override fun attack(target: Character) {
        if (inSleepMode) {
            println("║ $name находится в спячке и пропускает ход!")
            inSleepMode = false
            return
        }

        if (battery >= 5) {
            battery -= 5
            val damage = attackPower + Random.nextInt(1, 7)
            println("║ $name атакует ${target.name} и наносит $damage урона!")
            target.receiveDamage(damage)
        } else {
            println("║ Недостаточно батареи для атаки!")
        }
    }

    override fun heal() {
        if (battery >= 10) {
            battery -= 10
            health = (health + 15).coerceAtMost(maxHealth)
            println("║ $name восстанавливает 15 здоровья. Текущее здоровье: $health")
        } else {
            println("║ Недостаточно батареи для восстановления здоровья!")
        }
    }

    override fun chooseAction(): String {
        if (inSleepMode) {
            println("║ $name находится в спячке и пропускает ход!")
            inSleepMode = false
            return "0"
        }
        print("Выберите действие для $name: 1 - Атака, 2 - Лечение, 3 - Восстановить батарею, 4 - Последний удар: ")
        return readln()
    }

    override fun specialAction() {
        val damage = battery * 2
        battery = 0
        println("║ $name использует последний удар и наносит $damage урона! Он также впадает в спячку.")
        val target = chooseTargetForSpecial()
        target.receiveDamage(damage)
        inSleepMode = true
    }

    fun restoreBattery() {
        battery = maxBattery
        println("║ $name восстанавливает батарею до максимума!")
    }

    private fun chooseTargetForSpecial(): Character {
        val enemies = Game.characters.filter { it.isAlive() && it != this }
        return enemies[Random.nextInt(enemies.size)]
    }

    override fun printUniqueStats() {
        println("║ Батарея: $battery/$maxBattery")
    }
}

class Game {
    companion object {
        lateinit var characters: List<BaseCharacter>
    }

    private lateinit var knight: Knight
    private lateinit var mage: Mage
    private lateinit var robot: Robot

    private fun setupCharacters() {
        println("Настройка персонажей:")
        knight = createKnight()
        mage = createMage()
        robot = createRobot()
        characters = listOf(knight, mage, robot)
    }

    private fun createKnight(): Knight {
        println("Введите параметры для Рыцаря:")
        val health = promptForValue("Здоровье")
        val attackPower = promptForValue("Сила атаки")
        return Knight("Рыцарь", health, attackPower)
    }

    private fun createMage(): Mage {
        println("Введите параметры для Мага:")
        val health = promptForValue("Здоровье")
        val attackPower = promptForValue("Сила атаки")
        return Mage("Маг", health, attackPower)
    }

    private fun createRobot(): Robot {
        println("Введите параметры для Робота:")
        val health = promptForValue("Здоровье")
        val attackPower = promptForValue("Сила атаки")
        return Robot("Робот", health, attackPower)
    }

    private fun promptForValue(attribute: String): Int {
        print("Введите $attribute: ")
        return readln().toInt()
    }

    fun start() {
        setupCharacters()
        var round = 1
        while (characters.count { it.isAlive() } > 1) {
            println("═════════════════════════════════════")
            println("           Раунд $round")
            println("═════════════════════════════════════")

            for (character in characters) {
                if (!character.isAlive()) continue
                character.printStatus()
                val action = character.chooseAction().toIntOrNull() ?: continue

                if (action == 0) continue

                val target = chooseTarget(character)

                when (action) {
                    1 -> character.attack(target)
                    2 -> character.heal()
                    3 -> when (character) {
                        is Mage -> character.specialAction()
                        is Robot -> character.restoreBattery()
                        is Knight -> character.defend()
                    }
                    4 -> when (character) {
                        is Knight, is Robot -> character.specialAction()
                    }
                }

                if (character is Mage) character.passiveManaRegen()
            }
            round++
        }

        println("═════════════════════════════════════")
        println("           Игра окончена!")
        println("═════════════════════════════════════")
        characters.sortedByDescending { it.isAlive() }.forEachIndexed { index, character ->
            println("${index + 1}. ${character.name} (${if (character.isAlive()) "жив" else "мертв"})")
        }
    }

    private fun chooseTarget(attacker: BaseCharacter): BaseCharacter {
        val aliveCharacters = characters.filter { it != attacker && it.isAlive() }
        println("Выберите цель для атаки:")
        aliveCharacters.forEachIndexed { index, character ->
            println("${index + 1}. ${character.name} (Здоровье: ${character.health})")
        }
        print("Ваш выбор: ")
        val choice = readln().toInt() - 1
        return aliveCharacters[choice]
    }
}

fun main() {
    val game = Game()
    game.start()
}
