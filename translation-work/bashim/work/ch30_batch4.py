#!/usr/bin/env python3
"""Append chunk_30 records 301-400 (ids 412382-412481)."""
import json, os, re

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC  = os.path.join(BASE, "chunks", "chunk_30.jsonl")
OUT  = os.path.join(BASE, "out", "chunk_30.jsonl")

EN = {
"412382": 'tertuykalach: Don\'t know how to react to a subordinate\'s explanatory note signed "... With respeckt, Konstantin"',
"412383": 'Me: I\'m going to bake a cake, if you don\'t come help me, you\'re a traitor.\nHusband: Uh-huh\nMe: Does that mean you\'ll come?\nHusband: It means I\'m a traitor',
"412384": 'xxx: An applicant for the sysadmin position sent his resume in a doc file with a virus\nxxx: I consider that a serious bid for success :)',
"412385": 'xxx: all my life I was afraid of finding a spider in my bed. found one today. nightmare..\nyyy: and in the village they, like the locals, kept hinting to me that it\'s ME who\'s in THEIR bed..',
"412386": 'Heaven`s_A_Lie: fried eggs are a cook\'s hello world',
"412387": 'ххх: what do you do for work?\nууу: a teacher\nххх: English?\nууу: I do English',
"412388": 'Apple Mac hit by a new unknown virus attack\n\nxxx: is it late blight? or maybe powdery mildew?',
"412389": 'xxx: Many don\'t understand what the iPad is for. But after buying an iPad I don\'t understand what the iPhone is for.\nyyy: And after buying an iPhone I didn\'t understand what the iPhone was for',
"412390": 'Arctic: Looking at the movie posters and freaking out...\nKeeper: What?\nArctic: Kung Fu Panda 2, The Hangover 2, Pirates 4, Cars 2, Transformers 3, Fast Five, Mission Impossible 4, X-Men a fuckload, Harry Potter 7/2...\nArctic: Original ideas are just on the verge of extinction, though',
"412391": 'xxx: they\'re renovating the facade at our dorm, put up scaffolding by the building.\nxxx: sitting on the second floor, in my room by the window, watching a movie.\nxxx: then completely unexpectedly a dirty worker\'s face pops up in the vent window and asks: "you don\'t happen to have any jam? I\'m Karlsson...".',
"412392": 'контрабасист-единомышленник:\n\ndreamt that we now have a warm and fluffy kitty, and I\'m sleeping, gently pressing it to my chest. woke up, sadly realized we have no kitty. SUDDENLY realized I\'m still pressing something warm and soft to my chest.\nI open my eyes and discover it\'s my tits. :facepalm:',
"412393": 'ххх: Yesterday saw a white R8 in the center of Vladik! driving down Ocean Avenue... damn low, probably can only do laps around the center, and not everywhere :)\nууу: he apparently can\'t get out of it and just keeps driving in circles)',
"412394": 'It\'s tough when your mom is your boss.\n\nboss: on Sunday you need to be at the train station at 06:00 AM. Will you go?\nme: honestly, not really.\nboss: they might cut your July salary though. What do you say?\nme: I\'ll gladly go to the station, mom.',
"412395": '<@hamper_> just heard a scary word from one developer\n<@hamper_> багина',
"412396": 'yyy: Our new genius Java programmer called. Said a kitten bit him and he\'s in the hospital',
"412397": 'Discussion of an "insiders" event\nНаташа: why aren\'t you taking me with you!\nМаксим: we take each new member only on someone of ours\' recommendation + the fee is not small.\nНаташа: did I sleep with Vitalka for nothing! I should have a bunch of recommendations :)\nВиталий: yeah... slept... recommend',
"412398": 'gipsylilya@lj:\nThe other day on TV some Vika Tsyganova declared that a woman mustn\'t wear women\'s trousers, because in the afterlife God, attention, will mistake her for a man and make her do men\'s work.\nI used to think better of God\'s mental abilities.',
"412399": 'ebobat@lj: Peculiarities of northern business.\n\nSome guy calls me right now and starts the following dialogue:\n- Hello, Andrey. I received, never mind by what means, information that you\'re looking for contractors in Komi.\n- Suppose so. But personally it\'s important to me by what means this information reached you.\n- Well, one person told me, never mind his name.\n- Fuck... It\'s important to ME (emphasis on "me") who told you, you didn\'t read an announcement on a pole!\n- My engineer.\n- Very informative...\n- So, we\'re ready to consider your proposals.\n- We — who\'s that? What\'s your company called?\n- That\'s not important yet.\n\nbulitsyn:\nagree!\nthen you\'ll pay them some money\n\npo6om:\nwhich money exactly — that\'s not important yet',
"412400": 'xxx: it pisses me off when they say all of Russia feeds Moscow, Moscow besides having the biggest income also pays the biggest tax to the fed. budget\n\nyyy: and what does it produce? besides crap and waste paper.',
"412401": 'xxx: Nothing teaches calm like a few days with 64kbit\\s internet...',
"412402": 'xxx: how was the night?\nУУУ: don\'t know, I was asleep',
"412403": 'xxx: to develop yourself, you need to work in the real sector of the economy, not parasitize on resources.\nyyy: and what\'s that like?\nxxx: shoot porn, not supply girls for it.',
"412404": 'xxx: may you dictate emails to grannies over the phone for the rest of your life!',
"412405": 'xxx: those assholes who sit on the bench under my windows at 1 AM piss me off so much\nxxx: don\'t they have internet or what?',
"412406": 'Pivets_yada: And I think — it must be hard, watching and hiding porn, for the kids of sysadmins and all those tech support guys...',
"412407": 'She: Darling, I came up with a role-play game for us for the evening... and for the night\nHe: Already looking forward to it :) What\'ll we play? ;)\nShe: Doctor and patient ;)\nHe: Mmmmmmm... Kisa\'s imagination is running wild ;)\nShe: Close. Kisa\'s caught a cold.',
"412408": 'rikki-tikki: one familiar gentleman has such a cool and unusual James-Bond-like walk that all the ladies he passes on the streets stare. and it\'s all because in his youth an angry dog chased the gentleman, caught up and bit off a solid chunk of his ass',
"412409": 'ArkanoiD: The neighbor dug a pool and bought his kid an electric car. The result is somewhat predictable.',
"412410": '(after the rain)\nххх: Walked the dog. I\'m no dog person.\nууу: Why?\nххх: Because I\'m a swineherd!',
"412411": 'GLADIATOR: I knew of course that when guys meet someone they say the first thing that comes to mind, but the phrase: "miss, could you tell me how to go straight?" killed me\nSoegWind: well if the girl is 2 by 2 meters, that\'s a perfectly logical question',
"412412": 'Grazhdanin:\nToday I witnessed a really instructive example of resourcefulness.\n\nDriving a guy and a girl in the car. The girl has the nickname "Radio Mayak". Doesn\'t shut up for a minute. Chattered the whole way. Wore me out something fierce. The guy says:\n- Nastya, you\'re a smart girl, multiply 315 by 12...\n- Of course, I\'m smart...\nPause... Blissful silence. She multiplied for about five minutes, until it dawned on her she\'d been had. Got offended. The silence continued...',
"412413": 'From LiveJournal\nden_stranger: Nine in the morning! I step out on business and barely open the building entrance door when a sharp roar hits my ears, mixed with swearing and inarticulate screams. Judging by the sounds someone\'s being killed, very long and painfully. In shock I think about how to bypass the dangerous spot but still peek around the corner and there... a wedding. The bride ransom, damn...',
"412414": 'altteo: I found out my girlfriend had made a duplicate of my apartment keys when she proudly hurled them at me and left',
"412415": 'xxx: they left a kid with me for a week, to go gray with\nyyy: yeah, kids are like that. a couple days with them and you\'ll go gray',
"412416": 'xxx: Do you keep the blueprints for a weapon of mass burial at your place?\nyyy: A weapon of mass burial? A quarry excavator?',
"412417": 'xxx: What we didn\'t get enough of from our parents, life will definitely give us.\nxxx: Me, for example, I clearly didn\'t get enough whuppings.\nxxx: But life fixed everything!',
"412418": 'dk12k: overheard that Sber bought some Swiss bank\ndk12k: will there be queues and grannies there now too?',
"412419": '(Habr, from the discussion of the new paid mTorrentPlus)\n\nFakeFactFelis: You\'ll be able to download pirated content from pirate trackers with a pirated copy of the digital pirates\' protocol client\nahromenko: on a pirated Windows too ;)\nFinom: From Somalia\n4twilight: over a stolen dial-up.\nsolarfly: From a stolen PC, whose electricity is generated by slaves.\ncatsmile: ...stolen from the slave-owner neighbor.',
"412420": 'Was at a store the other day. A young man stands at the checkout, buying something, and blurts out:\nM: And give me a pack of Petr\nP: Which one?\nM: (in a stunned voice)Which one?! The First!',
"412421": 'xxx: I respect guys who can solve problems with one call. The main thing is that the call isn\'t to mom.\nyyy: So calling dad is okay?\nxxx: it\'s okay )\nyyy: "Hello, dad, tell mom that..." :)',
"412422": 'xxx: Punch a man in the face once — and his face will be broken for one day.\nxxx: Teach him to punch faces himself — and his face will be broken for life.\nxxx: Chinese folk wisdom.',
"412423": 'xxx: summer, half the bridges are raised, the illumination is gorgeous\nyyy: you\'re from Piter, at the sight of a raised bridge you shouldn\'t say "wow what beauty", but "fuuuuuck!"\nxxx: well I was tearing across the bridge like stung\nxxx: fast-fast\nxxx: only sneaking glances at the beauty :)',
"412424": 'Rin: there was a sign hanging in the room saying I need to do abs. walked past it — and felt ashamed every time, but never got around to it.\nbut! it happened.\ntoday I turned it to face the wall.',
"412425": 'xxx: that was a capital epic fail. I understand some people steal others\' work, but this guy came to the interview and started showing me MY OWN works in his portfolio!!! shitty 3D guy.',
"412426": 'xxx: How does your girlfriend feel about your trip to the strip club and Nastya?\nyyy: When she didn\'t know — fine.',
"412427": 'pauls: after the "Kazakhstan" receiver turns out to be the only one of your gadgets to survive a thermonuclear blast, you\'ll look at it with different eyes…\nsvb: burst ones?',
"412428": 'Ksenya Gavrilova: Watched Emmanuelle for the first time. The lady had it tough, wherever she turns — they fuck her everywhere',
"412429": 'from LiveJournal:\n\nI have a metalhead friend. A very vivid type: 190 cm tall, bearded, hairy, in leather and metal. Despite such a menacing look, a very pleasant person to talk to.\n\nRecently met up, and it turns out he\'s already married, and even has a kid.\n- Decided to get the kid into proper music from an early age... Played him "AC/DC" yesterday.\n- And?\nSighed:\n- Shit himself...',
"412430": 'xxx: the stairwell smells like someone dismembered a corpse and threw it down the garbage chute. And last week the local cop came by asking when I last saw the neighbors on the right. Coincidence?',
"412431": 'l13: Remember, the boss told you comrade S.\'s famous phrase "We have no irreplaceable people" when he was firing you?\nП: Yeah\nl13: He turned out to be quite replaceable himself.',
"412432": 'Милитаризм: I\'m at war with my neighbor. I live on the 6th floor, she\'s right above me, but on the 8th. When I go out to the balcony to smoke, the smoke apparently drifts up to her. For the last 2 weeks she\'s been pouring water down, hoping to hit me.\n\nМилитаризм: Well I just put a chair on the balcony and now I watch the systematically flying-by water from a safe place.\n\nМилитаризм: Yesterday I\'m sitting, smoking. I hear a girl on the 7th floor talking on the phone on the balcony. A bucket of water probably flies by. And a wild scream. From the 7th floor...\n\nМилитаризм: The neighbor who parks his car under my balcony will probably start washing his car himself again',
"412433": 'xoxol42: feels like the Sberbank ATM menus are written by quest developers. and the quests are different on different ATMs )',
"412434": 'xxx: My wife was teaching her mom to use the internet\nххх: The mother-in-law saw a scam about "we\'ll tell you everything about your personal file".\nххх: Wanted to find out.\nххх: My wife spent ages explaining that these are scammers doing SMS fraud\nyyy: How did it end?\nххх: My wife sent a 200-ruble SMS to prove they\'re scammers :)',
"412435": '<kafka> Yesterday I was at a friend\'s rehearsal spot at Rodeo Drive. On the door there\'s a notice with roughly this content: "Please don\'t have any kind of sex at the rehearsal spot. If there\'s no other way — at least don\'t leave condoms here. Otherwise I\'ll install cameras."',
"412436": 'yyy: My computer won\'t turn on, could you come over and fix it?\nxxx: I\'m a married man now and I only reinstall Windows for my wife. Look under the table and plug the plug into the socket yourself, please..',
"412437": 'xxx: Are there no mosquitoes under your blanket?\nyyy: no\nxxx: Will you let me in?;)\nyyy: the mosquitoes?',
"412438": 'Today a friend was complaining that her husband has been digging a well at the dacha for a month.\n- Why so long? — I ask.\n- You see, my husband dug the first meter in one go. Then the local priest was passing by and said that when you\'re digging a well, you mustn\'t swear — the water will be "dirty"... And that\'s it. The work practically stopped.',
"412439": 'rnt: sometimes you reread a bunch of forums, listen to all kinds of advice on repairing/replacing brake pads/cylinders/drums/shock absorbers. and after the repair you\'re happy as a kid. but you\'re happy not because you did everything, but because "finally I can now close THESE 20 PAGES IN FIREFOX"',
"412440": '(about the Bank Imperial commercial where women carried their armored husbands out of the city)\nthyl: but the shot where a woman tosses her armored husband on her back already hurt my eyes back then.\nSergey: there were strong women back then!\nthyl: seryozh, let\'s face the truth. how much does a grown man who professionally does war weigh?\nSergey: dunno ))))\nthyl: well 80 kilos minimum, I think\nthyl: now add the armor. it weighs, uhhh... let\'s say 30 kilos.\nthyl: in short, even if the man is light and the armor is light, this whole construction can\'t weigh less than 100 kg. the question arises:\nthyl: if these knights\' wives are so badass they toss more than a centner of weight on their backs, why don\'t they put on this armor themselves and chop all those konrads into small cabbage? )))',
"412441": 'ххх: At the intersection I crossed a black cat\'s path. I look back. The kitty stopped, thought and went around.\nууу: Looks like they have superstitions too',
"412442": 'xxx: So, did you deal with those who steal your laundry powder?\nууу: Yeah, I added "blueing" to the powder jar and the powder became just like in the ad, with "crystals of freshness" fuck. And I put the jar in the most visible spot in the utility room. Now two rooms in our dorm have become ardent fans of "Avatar", you can tell by the color of their clothes and their beastly faces :-)',
"412443": 'ART: Solntse I adore you\nFyria: okay, explain to me what\'s going on\nART: I fell in love\nFyria: with whom?\nART: With you of course\nFyria: okay, answer a couple questions. how old am I?\nART: 24\nFyria: when did we last talk?\nART: 4 months ago\nFyria: and why did you suddenly fall in love with me?\nART&#8206;: It\'s hard to put into words\nFyria&#8206;: fine, let me ask differently. what made you remember my existence at all after 4 months?\nART: Your green flower that lights up on my phone\nFyria: it seems to me that for such deep feelings as love, a green flower on the phone isn\'t enough\nART&#8206;: You broke my heart\nFyria: when?\nART: Just now\nFyria: with what?\nART&#8206;: You don\'t love me\nFyria: ????????',
"412444": 'ххх: I wonder, am I the only one whose brain explodes at the ad: "need a dress for an elven lady for rent, size 50."?',
"412445": 'My beloved (2 meters tall) got out of the shower, right then the doorbell rings. He goes to it. Me:\n- At least put on underwear!\n- As if!\nBehind the door a traveling salesman (1.55 m). Paying no attention to anything, he starts his familiar song:\n- Hello, I wanted to offer you...\nAnd then he realizes WHAT is in front of him. Goes numb. Runs away. Beloved (upset):\n- I should\'ve said: "Come here, sweetie..."',
"412446": 'xxx: a wheel knocked the kid down yesterday )\nyyy: what do you mean?\nxxx: I\'d already gone to bed\nxxx: a call — where are you, come pick me up from Spartak, a wheel knocked me down\nxxx: I drive over\nxxx: the kid was standing by the road, catching a taxi\nxxx: a six was coming down from Oktyabrskaya and turning onto Tashla\nxxx: on the turn its wheel unscrewed and flew off\nxxx: and it rolled downhill, and the kid had his back to it and didn\'t see\nxxx: the wheel knocked him off his feet, he hit his tailbone and head\nxxx: mild concussion',
"412447": 'ххх:\nAt work a girl is sitting, crying bitter tears. What\'s interesting — she\'s eating oatmeal cookies at the same time. I walk up and ask:\n- Why are you crying?\nThrough the wailing and sobbing:\n- Have you ever eaten cookies with pepper?\n- Then why are you eating it? Throw it out!\n- I can\'t. You know how taaaasty it is...',
"412448": 'xxx: do you think he\'ll agree?\nyyy: you\'d have to be a complete idiot to refuse such an offer\nxxx: that\'s exactly what I\'m afraid of, that he\'ll refuse',
"412449": 'xxx: Just went to the visa center to submit documents.\nxxx: There\'s a machine that gives out different queue numbers depending on why you came. Right in front of me a girl takes a queue number from the machine. Turns to the hall and shouts, who\'s last?\nxxx: In the middle of the hall another girl jumps up and shouts that she is.',
"412450": 'ххх: I always crack up at our bureaucrats who come up with sick leave forms and their format\nууу: what\'s up\nххх: in the "Other" line indicate the code: 31 - still sick, 32 - disability established, 33 - disability group changed, 34 - died, 35 - refused medical-social evaluation, 36 - showed up fit for work, 37 - follow-up treatment.\nххх: do they have to put "died" in the middle?\nууу: yes, it\'s the middle stage between sickness-disability and fitness for work',
"412451": 'xxx: The ant has the biggest brain of all living creatures. Relative to the body, of course.\nyyy: Judging by how fast these critters found the cookies, their intuition is pretty good too :)',
"412452": 'xxx: Today they asked me what bandana is hanging in the garage.\nxxx: A bandana.\nxxx: It\'s a pioneer neckerchief.',
"412453": 'xxx: You know why a mouse is a better gift than a turtle?\nxxx: Easier to tell whether it\'s dead or not',
"412454": 'xxx: My friend\'s dad works as a boss at Lukoil, achieved everything himself.\nxxx: And he gave his son a financial push in life in the form of an apartment, a car and stuff.\nxxx: In vain.\nxxx: Now my friend is flushing his life down that toilet...',
"412455": 'yantsen: You\'re a train driver and you\'re sad?\nyantsen: Send an SMS to the short number, change the boring dial tone to a cool melody!',
"412456": 'ххх: A big beetle flew into the room and fell somewhere near my bed. I\'ll sleep on a chair\nyyy: I\'d just leave the house, I hate insects. I\'m scared of them myself\nxxx: I wouldn\'t marry you! = two people sleeping on a chair isn\'t cute\nyyy: if I were your husband, we\'d have two chairs B-)',
"412457": 'xxx: whatever they say, it\'s not your boyfriend or even your husband who truly appreciates your cooking.\nxxx: the toughest judge in this matter is your beloved dad.\nxxx: because only he can say "you and your mother are alike, neither of you can cook for shit....."',
"412458": 'Аленка: Ehhh, it\'s great when the kid\'s at grandma\'s! No need to hide the candy and the vibrator!',
"412459": 'xxx: Warcraft.\nxxx: And your children will grow as fast as other people\'s.',
"412460": 'wlan> remember I wrote a diploma for Pots in May?\nwlan> he\'s calling today\nwlan> "Kiryukh, what was that diploma about? I\'m sitting at a job interview here, they\'re asking"\nwlan> SoulThreads: he defended it with flying colors',
"412461": 'xxx: The goal of the game is to build a castle and watch enemies die.\nyyy: Awesome! Some people\'s heads hurt more and more. Is there by any chance a game where everyone\'s alive, you need to save the Earth and watch it bloom, deserts disappear on it, it turns into a blooming beautiful garden???!!! What we play is what we get in life!\nzzz: First you gotta kill all the enemies!',
"412462": 'ххх: while the laptop reboots, decided to have some tea\nххх: from the kitchen I hear the characteristic melody — it rebooted\nххх: cutting sandwiches, I hear it — again\nххх: sitting having my meal, drank half a cup, suddenly the same sound again\nххх: it\'s beckoning, bitch',
"412463": 'xxx: How cool would it be to mold Rollton not into briquettes, but into, say, Terminators! You put it in a basin of boiling water and before your eyes plays out the final scene from the second part, when he was melting at the factory )',
"412464": '<xxx> opened the window\n<xxx> heard a strained scream from a quarreling couple "MAY YOU ONLY GET IT UP FOR TRANNIES!!!"\n<xxx> closed the window',
"412465": 'Roland:\nA dude came in, drunk to zero. "Me, bro, only 10 days out", all that. Gimme, he says, C.C. Catch, Modern Talking and ..uh.. Bad Boys Blue. And a bottle, motherfucker, can\'t do without it. I hand him a disc with mp3.\nHe: All five albums?\nMe: No, all nine.\nHe: Damn, I did good time...',
"412466": 'Medvedev wants to create a Russian World of Warcraft.\nA comment on the news.\n\nxxx: PARTY FOR BABA YAGA, NEED LEATHERWORKER, TWO ARCHERS, GRANNY-WITCH AND 2 DD IVANUSHKA-THE-FOOL, OR EMELYA 55+LVL, BRING REJUVENATING APPLES 2 STACKS MINIMUM, KOSHCHEI-PVP-SET OR CRABSET "PIKE\'S COMMAND", MEET AT THE FOREST EDGE IN 15 MIN.',
"412467": 'The OS gathered all the browsers and says sternly: Warning! Anyone who eats too much memory will immediately get punched in their smug red mug!',
"412468": '<kosta> with a UAZ you can\'t just "go-buy-install" anything. It\'s always a quest with an unexpected ending, in which, as a rule, one way or another an angle grinder figures.',
"412469": 'fess: Came for the ultrasound, expecting a baby.\nfess: After the doctor\'s words "you\'re having a girl", nothing smarter came to mind than blurting out "the fetus has cracked".',
"412470": 'PHANTOM: Sometimes it happens: you\'re silent, and you\'ve already been misunderstood.',
"412471": 'xxx: damn\nxxx: dad\'s a businessman. today at noon he quickly runs into the apartment, opens the freezer, hides a thick envelope deep inside and runs off\nxxx: well I think it\'s probably big money, or important documents, drugs in the end\nxxx: I open it\nxxx: and there are two melted chocolate bars (',
"412472": 'Sigurd: yeah I should learn to do such things myself\nSigurd: just need someone to explain =)\nSigurd: otherwise they\'ll write such stuff on forums, you\'ll kill your computer for sure\nVins: I have a solution\nVins: helps in 100% of cases\nVins: and you can do it yourself no problem\nSigurd: ohoho, hope this isn\'t another trolling =)\nVins: it is',
"412473": 'xxx: waking my daughter up for kindergarten, I say — need to open your eyes. She says "don\'t wanna, open my eyes yourself"\nxxx: we raised Viy',
"412474": 'From visitor reviews of a restaurant:\nVery tasty and filling. And everything would\'ve been great if my girlfriend hadn\'t seen a cockroach on the wall. A real cockroach was crawling on the wall and moving its whiskers. We called the waiter, he looked at it and said "good". What he meant by that I don\'t know.',
"412475": 'Sitting, playing WoW, mom\'s watching nearby. As I go I explain the point of the game and stuff to her.\nme: Right now we\'re killing a boss\nmom: And you\'re killing?\nme: No mom, I\'m a healer, I don\'t kill, I heal other players\nmom: Oh daughter, why didn\'t you go to med school!',
"412476": 'xxx: Bought myself a new computer, everything\'s super. Powerful, fast, quiet, but there\'s one downside...\nxxx: While it\'s shutting down, I don\'t have time to undress and get into bed :/',
"412477": 'Василиса:\nI work at a jewelry factory. Explaining: all manufactured items undergo "assay supervision" at the Assay Chamber; for short — test tube.\nA phone call to the company:\n\nX: - hello, the chief accountant please.\nme: - she\'s at the bank.\nX: - then the general director.\nme: - he\'s in the test tube.\n\nThe caller clearly couldn\'t reconcile what he heard with my serious tone.',
"412478": 'she: What are you up to?\nhe: growing hair on my palms.\nshe: how\'s that?\nhe: ruining my eyesight, and you\'re bothering me.\nshe: you\'ve been kind of strange lately, something happen?\nhe: yeah, broke up with my girlfriend.',
"412479": '"However, instead of doing their own development, the enterprise\'s employees downloaded scientific materials from the Internet and passed them off as their own know-how, pocketing the funds," reported the head of the GUEBiPK, police major general Denis Sugrobov.\nххх: what clever kids\nууу: And so grew up the generation of students downloading term papers and diplomas',
"412480": 'alenka: Please tell me, I already filed a marriage application at the registry office with a guy. Can I file another application at a different registry office with a different guy? And won\'t it come up in his presence that I already have an application filed?\nNix52: I did the same with my USE applications to unis, if they don\'t take me to one department, maybe they won\'t kick me out of another.\nГость: alenka, better file an application for an appointment with Goodwin, let him gift you brains and a heart',
"412481": 'rogue: I had no idea that contemplating a pregnant woman is so calming — as soon as I enter a subway car, all the sitting guys fall asleep',
}

EN = {int(k): v for k, v in EN.items()}

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
    rng = order[300:400]
    assert rng[0] == 412382 and rng[-1] == 412481, (rng[0], rng[-1])
    assert len(EN) == 100, len(EN)
    with open(OUT, "a", encoding="utf-8") as f:
        for i, rid in enumerate(rng, start=301):
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
