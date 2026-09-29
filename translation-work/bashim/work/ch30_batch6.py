#!/usr/bin/env python3
"""Append chunk_30 records 501-600 (ids 412582-412682)."""
import json, os, re

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC  = os.path.join(BASE, "chunks", "chunk_30.jsonl")
OUT  = os.path.join(BASE, "out", "chunk_30.jsonl")

EN = {
412582: 'xxx:\nIn the 90s my mom worked as a teacher at school - taught biology to sixth-seventh graders. Once they were turning in drawings about frog digestion. Among all the works there was a tattered sheet with a drawing and a description roughly like this: "a mosquito gets into the frog\'s mouth, then into the stomach, and then into the ass". Mom says dad begged her to give this boy an "A". She didn\'t.',
412583: 'Ann: I want to come visit you, but I can\'t (\nTim: why?\nAnn: money\nTim: honey!',
412584: 'xxx: a grasshopper just flew into my gym.\nxxx: 5 centimeters.\nxxx: with one back leg\nxxx: APARTMENT ON THE 3RD FLOOR!!\nxxx: HOW?!?!?!\nyyy: they threw it out from the 4th',
412585: 'xxx: sitting here now, looking out the window\nxxx: I see - a dead pixel, well I think, time to replace the window soon.\nxxx: and only then it dawned on me that there\'s a mosquito net there...',
412586: 'Shopping with my husband at the supermarket. I carefully take chicken eggs in a pack and hand them to him with the words: "Handle carefully, like your own)".\nIn response: "Should I scratch them?"',
412587: 'xxx: People really came to me, seriously assuming that renaming .bmp files to .jpg would really save a bunch of space on the file server.\n\nyyy: By the way, always dreamed of such a feature in an OS. Strange why it\'s still not done. Rename mp3 to .mkv – in a split second you get a video clip of your favorite song in HD-3D-DD5.1 quality, cry with happiness and kiss the monitor.\n\nxxx: :)\nxxx: - Hello, is this tech support? I recently renamed "War and Peace.txt" to "War and Peace.avi". And I immediately ran out of space on drive C!\n\nzzz: And can I get different actors?\n\nxxx: "War and Peace (with different actors).avi"',
412588: 'xxx> Daaaaamn, it feels so good to try for someone when they appreciate it!\nyyy> For example, for an employer...',
412589: '111: Got married?\n222: Nobody\'s taking me\n111: what are you waiting for?\n222: Waiting for mine to mature and build a house\n111: You\'ll wait like that, and then it\'ll turn out he also planted a tree and raised a son',
412590: 'Loki: my grandma in Zelenograd has a 3-room apartment, the sound insulation there - a neighbor from the first floor puts a tea bag in his mug and from the ninth they scream "you\'re fucking annoying, bitch!"',
412591: 'status: Washed the cats, now I have two cute porcupines\n\nМаришка:\nHow many RPM did you set for the rinse? )\n\nБо Йатигррррь:\nWhat do you mean, without rinsing, or they\'ll stretch. And at 30 degrees, otherwise the fur might shrink.',
412592: 'aaa: When there were no modems, corresponding pigeons were used to transmit packets.\nbbb: losses must\'ve been big )\nccc: yep, hackers used birds of prey to intercept packets',
412593: 'Glebus:...there was even a moment when I tried to explain to some ladies over the phone how to press the F5 key, which would magically solve their global problem...\n\n- Press the F5 button\n- We don\'t have such a button here, there\'s only "Print form" and "Search"\n- No, press it on the keyboard\n- Oh, I\'ll hand you the phone, you explain (hands the phone to her coworker)\n- We have a problem here (explains the issue)\n- I understand, I asked to press the F5 key on the keyboard\n- Where is it?\n- The top row of keys, right above the numbers\n- Aaa, file, edit, view, favorites...\n- No, don\'t look at the monitor, look at the keyboard, it\'s right under the monitor..\n- Oh, I don\'t understand, what is a keyboard? (Hands the phone to the first lady)\n- We have a problem here (explains the issue) ... and all over again.',
412594: 'xxx: Luzhkov, leaving, told Sobyanin that a treasure is buried under one of Moscow\'s sidewalks',
412595: 'xxx: I got sooo angryy today...\nyyy: And smashed the keyboard?\nxxx: Yep, howw did you knoww?',
412596: 'xxx: Nobody loves the admin while there\'s internet :(\nxxx: And when there\'s no internet - nobody loves the admin either for some reason ;(',
412597: 'lopata67: now in summer it\'s easier to find wi-fi than decent shade (',
412598: 'xxx: no, what are these locals like? I\'m walking drunk, asking how to get to the sea. They point their hand TOWARD THE MOUNTAINS! With the words "Just keep going up!". By the time I figured out where and how I\'d been screwed, I was lost.\nyyy: So you were so drunk you believed it?\nzzz: Swimming THAT drunk is life-threatening. They saved your life.',
412599: 'ххх: Yulchik... And if you were a man... And your wife, thoughtfully fiddling with your erect dick, said: "It\'d be nice to buy an electric meat grinder..." What would you think?',
412600: 'грабля: love people who work with humor!\nграбля: right now under my window they gathered to clean the sewer.\nграбля: opened the manhole cover\nграбля: and one of the workers loudly and joyfully blurts out: "fuck! it\'s all chocolate in here!"',
412601: 'From mail.ru answers:\n"I went into the start menu today and there\'s a computer shutdown option there. is it just me or can you turn it off without pulling it out of the socket? Four years using a computer, and only now saw this red button in start. it wasn\'t there before I think"',
412602: 'xxx: Real admins knit themselves sweaters from twisted pair\nxxx: With two screwdrivers',
412603: 'At the reception desk:\n- Without a city registration you can go paid - 2500 rubles, or collect the papers, then it\'ll be free, for 600 rubles.',
412604: 'xxx: you have no brother or sister at all?\nyyy: I do\nxxx: cousin\nyyy: brother?\nyyy: cousin sister, fuck',
412605: 'XXX: there was an AC installation here - you couldn\'t make it up\nXXX: The client\'s pipes are walled in, her father forgot to warn, they drilled through the pipe accordingly. Called a plumber, smashed the wall, welded it.\nXXX: But the plumber was drunk, fell off the stool, the torch fell on the couch, the couch burned down',
412606: 'From a forum.\nххх: What\'s the difference between Visa and Master Card bank cards?\nууу: I asked at Sber. Well what can I say... Service Russian-style. To the question how Visa differs from MC, they told me - on Visa there\'s a sunflower, and on MC a soccer ball.',
412607: 'MoZgOFF: the 29th was a toast to fishermen... They also have nets, ports, and gateways!',
412608: 'Михалыч: we simply need a superhero - Admiral Rozental )\nМихалыч: to burn the basics of literacy into young brains with fire and sword',
412609: 'xxx: What a surname is to an American, a nick is to a Russian.',
412610: 'gingerlynx: kitty, I bought myself a laptop\ngingerlynx: it\'s cool\ngingerlynx: big screen and a shitload of memory\ngingerlynx: but the coolest thing isn\'t that\ngingerlynx: ITS KEYBOARD IS MADE SO CRUMBS DON\'T FALL INSIDE',
412611: 'xxx: Mom named the fly Vasily and demands that I kill it. I don\'t know now (',
412612: 'Talking in a group of friends:\n\nхxx: Twins study in my group.\nуyy: Pretty?\nxxx: Well, one of them definitely is...',
412614: 'YED: Mda... IT people, RPG fans - they\'re simply invincible people!\nYED: You remember the scandals with Yandex?\nYED: So against this background, a friend of mine accidentally used Yandex to find out that someone was delivering flowers to his wife through an online store. And my buddy was on a business trip at the time.\nYED: Tracked down the admirer via email in a social network...\nYED: In short, now he\'s divorcing...\nSiXX: daaaa. fun RPG in real life!',
412615: 'xxx: caught myself thinking that I\'m destined not just to find my soulmate, but, fuck, to assemble a damn puzzle...',
412616: 'Comment on the movie "Salamander Key":\nGripping plot, well-crafted dialogue, excellent humor, wonderful acting. That\'s exactly what this movie desperately lacks. Only Emelianenko pleased with his acting. It\'s so rich, emotional and deep that I immediately remembered Chuck Norris at the peak of popularity.',
412617: 'www: The whole history of women\'s fashion is the history of the struggle of climate, morality and hygiene against a woman\'s desire to walk around naked.\nxxx: let them walk around naked, who cares\nyyy: and if your girlfriend walks the streets naked, how would you like that?\nzzz: Well let the single ones walk naked, and the taken ones dressed. That\'d rid the world of many problems at once',
412618: 'ххх: Fuck, got blisters from the shovel yesterday.\nууу: Why don\'t you wear gloves?\nххх: Don\'t like to.\nууу: What, doesn\'t feel the same?',
412619: 'Volgograd. Temperature 40+.\nWas welding in a meter-high attic under a metal roof. After an hour climbed out into the street. What I\'m getting at...\nGuys! +40 is COOL!!!',
412620: 'I have a buddy who repairs cars (his own service in a garage). And he needed to polish the torpedo (front panel in the cabin) to a shine. And back then all the modern products were a novelty and cost quite a lot. And he decided to do it with regular vaseline, for which he went to the pharmacy.\nB(uddy): Give me vaseline for 20 rubles please...\nP(harmacist): What for?\nB: To rub the torpedo!\nP: Better take the one for 34 rubles\nB: What\'s better about it?\nP: It\'s warming...',
412621: 'xxx: I just can\'t treat normally people who are named the same as my cat.',
412622: 'xxx: The older I get, the smaller pelmeni seem to me',
412623: 'ххх: Hi, what are you doing?\nууу: Playing with my pussy...\nууу: I mean the cat!!!\nууу: I know what you thought you goat!!!!\nUser "ууу" removed you from their friends list!',
412624: 'xxx: what about the army?\nyyy: Not drafted due to family circumstances.\nxxx: what kind?!\nyyy: Mom works at the draft office.',
412625: 'demeneonic_01: Who first started using tracked-wheeled armored protractors?\nMobile02: Every time I picture this wonder-protractor I slide under the table. :D',
412626: 'xxx: I\'m so naive and trusting\nxxx: especially when they\'re trying to undress me\nxxx: in those moments it seems to me that they\'re about to confess their love',
412627: 'Standing with grandma in a store, I\'m bragging:\n\nMe: My kitty (boyfriend), when I\'m working, cooks me chicken legs so tasty!\n\nA woman nearby with a stunned look: HOW? HOW DID YOU TEACH A CAT TO COOK?',
412628: 'Irina : want to sleep (\nAlex M: and what were you doing at night?\nIrina : at night - we were sleeping, but it was kind of short (\nAlex M: you probably slept too fast',
412629: 'xxx: mysqli_real_connect(): MySQL server has gone away\nxxx: Should I go chase it now?',
412630: 'xxx: In the couch armrest, in a fold of the upholstery, lay a couple of condoms, since there\'s no nightstand, and this way they\'re always at hand...\nxxx: So answer me, where do women get this habit: sticking needles into the upholstery while sewing?!',
412631: 'xxx: need something for tea for the evening\nууу: soup?',
412632: 'Серёга: just bought shampoo, it says it nourishes hair and scalp FROM THE INSIDE!\nСерёга: looks like I\'ll have to drink it',
412633: 'xxx: Remembering how much I started learning and quit, I\'m thinking of writing about myself in my resume as "diversely underdeveloped".',
412634: 'xxx: I want to make nitrous oxide for my car, since I\'m a beginning street racer, buying it is very expensive. Who knows, give me a recipe to make at home.',
412635: 'xxx: how can you sleep on this beautiful night, filled with the sounds of rain and thunder... as well as dogs barking at the thunder, cars answering the thunder with alarms, and drowning them out the scream "shut the fuck up already", probably addressed to the car or the dogs',
412636: 'Miss: a question that came to our site: hello! there\'s a HIGH-pressure gas pipeline near our house. tell me, can I connect to it MYSELF? thanks, Nazira.\nMiss: Of course, Nazira! Grab an angle grinder - and go!',
412637: 'Torrent release: Postal 10 years. Anniversary edition\nGenre: Action/postman life simulator',
412638: 'xxx: Modern technology, fuck.\nxxx: Today I asked for "Cream-Blu-ray" ice cream',
412639: 'S&F: Snake, hill and parking - that\'s easy, but tasks like: "get out of the fucking courtyards in khimmash without GPS" and "don\'t wake the drunk instructor friend driving on the back seat so he doesn\'t puke" - that\'s something new',
412640: 'xxx: Spilled cola on the laptop\nxxx: Even a Kalashnikov wasn\'t disassembled that fast in the army',
412641: 'xxx: We\'ll create our own blackjack\nxxx: with bl..\nxxx: fuuuuuck',
412642: 'xxx: The height of cynicism on his part was watching My Little Pony while eating horse sausage',
412643: 'Alex: Love my company. The boss comes to the head of the development department.\nBoss: so how\'s the situation in one word?\nHead: working!\nBoss: and in two words?\nHead: not working!',
412644: '<Элька> it\'s 4 AM for me:)\n<Tehhi> it\'s 3:10 for us\n<Kronik> 11 AM\n<Kon> 4.06\n<Tehhi> clock sync :))\n<Tehhi> (failed)',
412645: 'xxx: I remember a wonderful moment, You appeared before me, Then crits for 220, Then the cops found my corpse.',
412646: 'Доктор911:\nI believe that whoever first swore at his tribesman instead of killing him became the progenitor of civilization. Therefore even to the most insignificant fight I will prefer such choice swearing that your eyes will flow out and your arms will fall off',
412647: 'xxx: I took off the cast, now I can play dota )\nzzz: did you straighten your arms?',
412648: 'xxx: she sincerely believes she\'s a vampire.\nyyy: do you give her blood when you meet?\nxxx: no, but recently she wouldn\'t let me kill a mosquito, saying it\'s her "fellow"\nyyy: take a closer look at her - maybe she\'s actually a mosquito?',
412649: 'Nazgul: Went to an admin meetup near Kaluga.\nNazgul: All Saturday evening we were arm-wrestling - now my arms won\'t bend or unbend.(\nStuSha: At least nobody can blame you for them being crooked :-)',
412650: 'yyy: something strange just happened\nyyy: stepdad is flying around the apartment looking for a needle\nyyy: mom asks - what for?\nyyy: he says - to sew up my ass\n...\nyyy: turns out he\'s cooking chicken, and the lemon keeps falling out)',
412651: 'xxx: Fuck him and his stories.\nxxx: All his stories start with the words "I wake up with a terrible hangover" and must contain the phrase "And then I realize I\'m fucked".',
412652: 'xxx: tomorrow we\'re drinking at my place\nxxx: I\'ll provide the table, you provide the booze )\nyyy: can I also bring the table instead?',
412653: 'ххх: I\'m not going south with you\nyyy: are you an idiot? the bus is tomorrow!\nххх: my dad found morning searches "liver regeneration" and "liver vitamins" in the google search history :(',
412654: 'aaa: you\'re such a handsome red-bearded guy. You should get a red plaid shirt and you\'d be just like an American lumberjack ))\nббб: well buy one\nааа: yeah... and we\'ll play role-playing games... the lumberjack and his log xD',
412655: 'from a forum\nxxx: tell me, what to do with last year\'s blessed eggs? How to properly dispose of them? Can I bury them?',
412656: 'Katrin (Black\'и): outside my window a car alarm is howling in different voices\nKatrin (Black\'и): and a girl coworker is howling along with it\nKatrin (Black\'и): I\'m working like I\'m in a madhouse :(',
412657: 'Scene: Moscow City, a new glass 57-story high-rise, one of the coolest restaurants in the lobby.\nVisitor (V): Miss, does your restaurant have Wi-Fi?\nWaitress (W): Yes, it does. (confidently)\nV: And what\'s the login/password?\nW: You should know that yourself...\nV: ...am I a telepath?\nW: Well you should have wifi with you...\nV: and what do you have then?\nW: well we have wifi, it catches, but only if you bring it with you...',
412658: 'News: A team of treasure hunters discovered an object resembling a UFO on the ocean floor - in the shape of a perfect circle 18 meters in diameter.\n\nComment: Don\'t you dare lift it! It\'s a plug like in a bathtub, lift it and all the water will drain out!',
412659: '<xxx> My friend fell off the barn and into a pancake...\n<yyy> He crashed?!\n<xxx> Into a cow pat',
412660: 'he: yeah, on weekends as usual with friends around the night city, on weekdays work, I\'m almost never home\nshe: and how are things with your girlfriend?\nhe: well, we broke up\nshe: she kicked you out for non-attendance?',
412661: 'xxx: there\'s a computer next to the TV at grandma\'s. Something\'s wrong, the TV won\'t turn on today. Grandpa comes up, looks, says: "It\'s probably a computer virus in it"\nyyy: :D\nxxx: - Me, - Like how?\n- Him, - Well through radiation\n- And what did it do?\n- Chewed through all the wires inside.',
412662: 'yyy: And what happens, for example, if you piss into a working TV?\nxxx: Don\'t know, but you\'ll survive the zombie apocalypse no problem.\nyyy: Why\'s that?\nххх: Because, my friend, YOU, FUCK, HAVE NO BRAINS!',
412663: 'yyy> a buddy told me how at 16 he wanted to kill himself over unhappy love. It was autumn, and he decided to throw himself off the balcony. He came home, and the relatives had already sealed the balcony for winter. And he got too lazy to unseal it...',
412664: 'xxx: Guys, good news.\nyyy: we\'re fired?',
412665: 'xxx: Fuck minecraft, I found my Lego box!',
412666: 'about graduates\nхxx: And Diman sat and sat, then said "okay guys, I\'m kinda tired, going to sleep" and left. And the next day he moved out of the dorm and went straight into the army.\nxxх: took French leave',
412667: '[111]: discount: if you get a full leg depilation - mustache+armpits as a gift!\n(punctuation preserved)\n[111]: you leave the salon all beautiful: with smooth legs, a mustache and 4 armpits )',
412668: 'netmocka:\nNikitka is a good lad of course ) Yesterday he got drunk to hell, didn\'t cause trouble or anything.. Quietly lay down on a bench and whispers - "Guys, guys, come here please."\n\nLeva:\nhe\'s off again?)\n\nnetmocka:\nyeah, you said it ) He\'s lying there and says - "Guys, when I die... will you come to my funeral..." - and then in a pitiful voice - "bring a bird\'s milk cake, guys..."',
412669: 'Boss: come on, faster already!\nEngineer: what\'s faster?\nBoss: don\'t remember. But faster!!!',
412670: 'ххх: Fuck, why is the tap water flowing lukewarm, like fresh milk?!\nххх: After it got cloudy outside these past days, I can\'t shake the thought that our city\'s hot water heating isn\'t done at the power plant, but solely on those sections of the heating main that are laid above ground, using solar heat.',
412671: '- I know why buttons on men\'s and women\'s jeans are on different sides!\n- ???\n- So it\'s convenient for everyone to pull them off each other!',
412672: 'girl: damn, why haven\'t they invented new gasoline for like 13 years already!!\nMe: Why 13 years?\ngirl: well damn, gas stations only have 98 and nothing better! it was invented in 1998!! 13 years ago!!',
412673: 'Сергей : guys, guys, have you read about selling clothes for real money in diablo3?\nСергей : I finally found my dream job!',
412674: 'xxx: A guy at work constantly writes stuff on his palm so he doesn\'t forget... Like "buy milk" etc.\nAnd today I saw the inscription - "Save the Emperor"!\nyyy: Looks like our world is in for changes :)\nxxx: Turned out, it\'s a movie he wants to download.',
412675: 'xxx: Broke my left arm three days ago\nxxx: The fracture isn\'t as scary as the Diamond Arm jokes',
412676: 'xxx: To the senior investigator of the first department for investigating especially important cases (on crimes against the person and public safety) of the investigative directorate of the Investigative Committee of the Russian Federation for the * region – to the investigator of the investigative group of the Main investigative directorate of the Investigative Committee of the Russian Federation for the * district, lieutenant colonel of justice\n*in I.O.\nxxx: quite a job title\nxxx: I\'m composing a letter and this is the header in the letter\nyyy: Vadim and I concluded that his least favorite question is: "what do you do for work?"',
412677: 'Danya: A new understanding of the full benefits package has spontaneously formed: open internet, real vacation and air conditioning.',
412678: 'xxx: take note: a kitten is not capable of eating more than can fit in your refrigerator!',
412679: '<xxx>: he arranged a wonderful romantic evening for me\n<xxx>: with a walk in the park under the moon, dinner by candlelight and rose petals\n<xxx>: when I was already ready to give myself to him (taking off my bra)\n<xxx>: his cell phone rang\n<xxx>: he answered, muttered "yeah" a couple times with a bored look and hung up\n<xxx>: and then he said: let\'s just stay friends?',
412680: 'xxx: and do testers get separate wards in the madhouse? )\nyyy: with hard spots in the walls - you have to find the bug in the soft padding',
412681: 'ххх: "Safe speed" is when the car\'s speed doesn\'t exceed the speed of the driver\'s thoughts.',
412682: 'xxx: Does it happen to you that you walk into a room and just don\'t remember what you wanted to do?\nyyy: yeah\nxxx: I have a feeling that\'s how dogs spend their whole lives.',
}

def main():
    src = {}
    order = []
    with open(SRC, encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if not line.strip():
                continue
            r = json.loads(line)
            src[r["id"]] = r["text"]
            order.append(r["id"])
    assert len(src) == 1000, len(src)
    rng = order[500:600]
    assert rng[0] == 412582 and rng[-1] == 412682, (rng[0], rng[-1])
    assert len(EN) == 100, len(EN)
    with open(OUT, "a", encoding="utf-8") as f:
        for i, rid in enumerate(rng, start=501):
            assert rid in EN, rid
            ru = src[rid]; en = EN[rid]
            assert en.strip(), rid
            assert en.count("\n") == ru.count("\n"), (rid, "newlines")
            toks = re.findall(r"<[^<>\n]*>", ru)
            for t in toks:
                assert t in en, (rid, repr(t))
            f.write(json.dumps({"id": rid, "ru": ru, "en": en}, ensure_ascii=False) + "\n")
    print("OK: appended 100")

if __name__ == "__main__":
    main()
