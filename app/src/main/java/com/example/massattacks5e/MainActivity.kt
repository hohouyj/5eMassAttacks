package com.example.massattacks5e

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import java.util.*
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val rollButton = findViewById<Button>(R.id.rollButton)
        val totalDamageView = findViewById<TextView>(R.id.totalDamageTextView)
        val numHitsView = findViewById<TextView>(R.id.numHitsTextView)
        val numCritsView = findViewById<TextView>(R.id.numCritsTextView)
        val numMissesView = findViewById<TextView>(R.id.numMissesTextView)


        data class Attack(
            val targetAc: Int,
            val toHit: Int,
            val damageList: List<String>,
            var hitBoolean: Boolean,
            var critBoolean: Boolean,
            val advBoolean: Boolean,
            val disadvBoolean: Boolean,
            var damageDealt: Int
        )

        fun rollDice(dieMax: Int): Int {
            return Random().nextInt(dieMax) + 1
        }

        fun rollAttack(attack: Attack): Attack{
            val adv = attack.advBoolean
            val disadv = attack.disadvBoolean
            val toHit = attack.toHit
            val atkRoll =
                if(adv && disadv){
                    rollDice(20)+toHit
                }else if (adv){
                    max(rollDice(20)+toHit,rollDice(20)+toHit)
                } else if (disadv){
                    min(rollDice(20)+toHit,rollDice(20)+toHit)
                } else {
                    rollDice(20) + toHit
                }
            if ((atkRoll-toHit)==20){
                attack.critBoolean = true
                //attack.hitBoolean = true
            }
            if ((atkRoll-toHit)==1){
                attack.hitBoolean = false
            }
            if (atkRoll>=attack.targetAc){
                attack.hitBoolean = true
            }
            return attack
        }

        fun calcDamage(attack: Attack): Attack{
            var damage = 0
            if (attack.hitBoolean || attack.critBoolean){
                val damageLs = attack.damageList
                for (dmgStr in damageLs) {
                    if (dmgStr.contains("d", ignoreCase = true)) {
                        val dmgStrList = dmgStr.split("d")
                        val numDice = dmgStrList[0].toInt()
                        val dieMax = dmgStrList[1].toInt()
                        repeat(if (attack.critBoolean) numDice * 2 else numDice) {
                            damage += rollDice(dieMax)
                        }
                    } else {
                        damage += dmgStr.toInt()
                    }
                }
            }
            attack.damageDealt = damage
            return attack
        }

        rollButton.setOnClickListener{
            val ac = findViewById<EditText>(R.id.acEditTextnumber).text.toString().toInt()
            val toHit = findViewById<EditText>(R.id.toHitEditTextNumber).text.toString().toInt()
            val numAttacks = findViewById<EditText>(R.id.attacksEditTextNumber).text.toString().toInt()
            val advantageBool = findViewById<CheckBox>(R.id.advCheckBox).isChecked
            val disadvBool = findViewById<CheckBox>(R.id.disadvCheckBox).isChecked
            val damagePerAttack = findViewById<EditText>(R.id.damageEditText).text.toString().replace(Regex("[^dD0-9+]"),"")
            val damageList = damagePerAttack.split("+")
            var numHits = 0
            var numCrits = 0
            var totalDamage = 0

            var isMultiAttack = findViewById<CheckBox>(R.id.multiAtkCheckBox).isChecked
            var multiAttackDmg = findViewById<EditText>(R.id.multiAtkDmgEditText).text.toString().replace(Regex("[^dD0-9+]"),"")
            var multiAttackDmgList = multiAttackDmg.split("+")


            repeat(numAttacks){
                var attack = Attack(ac, toHit, damageList, false, false, advantageBool, disadvBool, 0)
                attack = rollAttack(attack)
                attack = calcDamage(attack)
                numHits += if (attack.hitBoolean) 1 else 0
                numCrits += if (attack.critBoolean) 1 else 0
                totalDamage += attack.damageDealt
                if(isMultiAttack){
                    var secondAttack = Attack(ac, toHit, multiAttackDmgList, false, false, advantageBool, disadvBool, 0)
                    secondAttack = rollAttack(secondAttack)
                    secondAttack = calcDamage(secondAttack)
                    numHits += if (secondAttack.hitBoolean) 1 else 0
                    numCrits += if (secondAttack.critBoolean) 1 else 0
                    totalDamage += secondAttack.damageDealt
                }
            }
            numCritsView.text = numCrits.toString()
            numHitsView.text = (numHits-numCrits).toString()
            numMissesView.text = if(isMultiAttack) (numAttacks*2-numHits).toString() else (numAttacks-numHits).toString()
            totalDamageView.text = totalDamage.toString()

        }
    }
}