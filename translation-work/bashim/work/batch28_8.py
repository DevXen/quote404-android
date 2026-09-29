#!/usr/bin/env python3
"""Append English translations for bash.im chunk 28, records 701-800."""
import json, os

BASE = '/home/hatch/workspace/translation-work/bashim'
srcp = os.path.join(BASE, 'chunks/chunk_28.jsonl')
outp = os.path.join(BASE, 'out/chunk_28.jsonl')

EN = {
410779: """xxx: But really it's better to trust your own experience, based on common sense. Like in the smoothbore section among the handloaders one comrade advised another to grind gunpowder in a coffee grinder for a sharper shot, and he believed it and without thinking threw the powder into an electric coffee grinder. Good thing there wasn't much of it.""",
410780: """xxx: Mother-in-law called with a lecture on "If you want to conceive a baby, you must not do it during Passion Week, or you'll get some kind of monster".
xxx: For fun we calculated when such a baby would be born...
xxx: Turns out the mother-in-law herself was conceived during Passion Week.""",
410781: """ЫЫЫ: Standing in the tax office. In front of me an uncle is dictating something inarticulately, a very young employee asks: "Repeat by letters".
ЫЫЫ: Well, he starts. Something like: A - Arkady. R - Roman. Then paused. "Y - Yod".
ЫЫЫ: The girl thought: "What? Say the name!"
ЫЫЫ: He says to her: "Y-O-D".
ЫЫЫ: "What kind of name is that? - the girl asks. - Say it clearer".
ЫЫЫ: And then the uncle snapped: "It's fucking shame. F_ucking!!! Y! Yod, YOG!!!"
ЫЫЫ: The girl raised her eyebrows into a little house and knowingly said: "yo? You should've just said YOZH, for example".""",
410782: """xxx: when buying a huge cleaver in a store, to the seller's question "For what purposes?" it's usually easier to answer - I'LL BE KILLING PEOPLE, than to explain what cosplay is.""",
410783: """Eiroj: Dad, stop shouting through the whole apartment, go into ICQ and say it normally.""",
410784: """xxx: painted the entrance door, thanks.
xxx: hung a warning, thanks.
xxx: but painting the handle is beyond good and evil.""",
410785: """From a women's forum.
xxx: What kind of stupid logic is "if you don't like it you must be jealous?" I don't like bums, so I'm jealous of them?""",
410786: """xxx: the world is full of idiots
xxx: our dacha was robbed
xxx: well, robbed
xxx: an ancient fridge, old tools, random carpets, A BED!!!!
xxx: all the stuff that should've been thrown out )
xxx: people pay big money to have their garbage taken away
xxx: and they didn't even ask for thanks
xxx: I'll never stop being amazed at human stupidity""",
410787: """xxx: The HR lady accidentally deleted a folder, ran in: "Restore!" Said the backup will only be yesterday's. The aunt asked when the backup is made and how. Was surprised, showed, explained the script runs every night. The HR lady said she needs today's data, and she'll wait until tomorrow when today's backup is made.""",
410788: """xxx: A not-quite-ironed shirt can conditionally be considered freshly crumpled.""",
410789: """xxx: Love my country...
xxx: In winter the cat slept on me because the radiators were cold and she was freezing.
xxx: And now she sleeps on me because the radiators are burning hot and they burn her paws =(""",
410790: """xxx: Spring, damn
xxx: The upstairs neighbors who kept having drunken brawls are now yelling not over my head but under my windows :-(""",
410791: """xxx: A kid once asked grandpa "Grandpa, what kind of computer did you have?"
xxx: Our grandpa said "I'll show you", and then went and got an abacus from the basement""",
410792: """<CAHEKs> today was taking an informatics exam, to the question what Windows is for, a student answered:" well it can be updated... and reinstalled...\"""",
410793: """zem: Half a day the cat walks around all confused, sad, somehow offended. only in the evening understood why... they turned off the heating!""",
410794: """xxx: Our old men decided to build a shed.
xxx: Nobody, of course, knows how to build it. Gathered: my father, his brother, one more not very distant relative, a neighbor. All 40-50+, almost all have heart problems, etc. Who builds? Of course me, a 20-year-old dumbass. Seems they were preparing me for adult life - 5 bosses who don't know what to do and a worker who's forced to work anyway""",
410795: """yyy: cooking forums fill me with tenderness, baby-talk and glitter
yyy: and where do they get these billions of disgusting GIFs with bears
xxx: by the way, I always come across some solid discussions on cooking forums with strict design.
xxx: that's probably because year after year I google about cooking only "how long to boil pasta" and "what water to throw pelmeni into". truly male territory, where bears cannot exist.""",
410796: """xxx: Moving to a new job, I made an unpleasant discovery: I have very average mental abilities. It's just that before I was surrounded by clinical idiots and I stood out favorably against their background.""",
410797: """masterstop: I did the fastest disk format in the world
masterstop: 1 TB per second
starcin: Smashed your drive on the floor?
masterstop: Think flatly, dad ran over it with his Kamaz
masterstop: and don't ask how it got there""",
410798: """LLlypuK: however, painting a ceiling with acrylic paint in a 3 by 4 meter room with a sealed window and closed door is such a dizzying activity...""",
410799: """From a work report:

The number of cats inhabiting this house defies counting, the stench is unbelievable. Smelled up to I won't write what. On leaving the house was taken by a passing pack of dogs for a big stinky cat. Sat on a tree for an hour and a half until they got hoarse. Request to add to the electrician's toolkit a gas mask and self-defense means.
PS I'll bring clothes to wash. And the bag. While I was changing the meter - they marked it.""",
410800: """xxx: I never finished Kafka's "The Castle".
yyy: Don't worry, he never finished writing it""",
410801: """<xxx> Facebook asks what I'm thinking. Twitter asks what I'm doing. Foursquare asks where I am. This internet of yours behaves like my ex!""",
410802: """xxx: I watched movies with Hayden Christensen for three days, now I still feel like he's my neighbor.""",
410803: """xxx: I'm a werewolf!
xxx: Exactly a week before my girlfriend's period, I magically turn into a goat )""",
410804: """xxx: Almost a century we strived for a global idea - life without money; from each according to ability - to each according to need and so on.
xxx: And finally it happened, we built...
xxx: No, not communism of course, Torrent""",
410805: """xxx: one of my acquaintances has rose-colored glasses built into his retina
xxx: three years running errands
xxx: still firmly believes a cool career awaits him...""",
410806: """ххх: my mother proposed to my father on April 1st. Father agreed. For 38 years they've been wondering who actually fooled whom...""",
410807: """xxx: Barack!
xxx: Downloaded oil - get on distribution!""",
410808: """xxx: I actually used to want to be a writer
xxx: already after I realized I won't get to be Gadget )
yyy: why won't you get to be Gadget? )
xxx: my dreams were shattered by reality
xxx: I realized I won't fit in a ketchup bottle""",
410809: """xxx: Love is when you argue over a tea bag, and then fall asleep hugging

First comment:
yyy: with the same tea bag""",
410810: """At a probability theory lecture:
The thing is, Soviet light bulbs, that filament wire itself, was made of tungsten, and now it's made of shit. The difference between shit and tungsten is that tungsten burns for 10 years, and shit burns for 1 month.""",
410811: """xxx: Today at Sberbank.
xxx: A voice from the speakers announces: "Client number 666, please approach window number 6".
xxx: I thought: "No one will approach".
xxx: No one approached.""",
410812: """МегаБайт: Listen what happened to me in the evening.
МегаБайт: Was hanging a satellite dish for a client. Client lives in a private house. Anyway, a piece of tile went out from under my feet and I, with a satfinder in one hand and a wrench in the other, didn't manage to grab anything, but caught my pant leg on the TV antenna, and it (the pipe itself) was rotten at the base and broke off. I fell first onto an apple tree branch and it broke off, then hit my back very hard on the fence and flew at full speed into the dog kennel. Kennel in splinters. The dog apparently died of fright (I really didn't want that!). And with my foot I managed to break the glass in the window (while flying I wanted to regroup but didn't make it). Now their roof also leaks. And there's no dog. The yard is a mess like after a rocket strike, no internet and the zombie box doesn't show. I'm a top-class professional. That's what it says on my business card.""",
410813: """- Temik, when you go to Serega's village, definitely take rubber boots or shoes you don't mind, it's wet and muddy here.
- Is it really that bad?
- Well... basically, yes. His ducks are swimming across the field here.""",
410814: """From LJ:
Came across an unclouded formulation of the plot of the movie "Stalker": "A film about three men walking through wooded terrain, two walk almost calmly, and the third is constantly afraid of something\".""",
410815: """xxx: Watching the movie "Morozko" with a kid - the scene where Ivanushka and Baba Yaga take turns commanding the hut and it spins in different directions.
xxx: Kid - "The hut has no protection, any Ivan the fool with admin rights..\"""",
410816: """xxx: just turn on porn, and you suddenly and unexpectedly become needed by everyone""",
410817: """Renogor: and about song lyrics I won't even say
Renogor: used to be lyrics were lyrics, and now - you didn't give to me, I didn't give to you, you're a fool, and you're annoying as fuck
Renogor: I think I just wrote a hit""",
410818: """Story from a taxi driver.
taxi driver: Arrived on call, picked up a lady and went with her to meet her husband at the railway station. Met the husband with a little son, driving back. The son struck up a conversation with me - me with him. Discussing the comfort of traveling in a compartment. The son says: "And I had a good ride. Warm and lots of space. I slept alone on the top bunk, and dad with aunt Nina - on the bottom. Together..." Drove on in silence.""",
410819: """Just now the home phone rings. Mom picks up:
Male voice: Hello, is your mom home?
Mom: But I AM ��ама.
Short beeps...""",
410820: """liveund: why are you moderately religious?
liveund: you're an engineer, how can you believe in supernatural forces?
*Homer*: sad to believe that everything depends on that fuckup in the mirror""",
410821: """xxx: my subject folders are named either by discipline or by teacher, and one is just called "uncle" because I know neither""",
410822: """Insulin: Growing up is when at the dentist you start fearing not the pain, but the bill.""",
410823: """nothing_: any correspondence students here?
Vladic: if you don't go to uni for two weeks, can you consider yourself a correspondence student?
sonics_: you can consider yourself a draftee""",
410824: """xxx: Why so gloomy?
yyy: Fuck this "Advocate of healthy lifestyle". Proteins, fats, carbs, calorie counting, fiber, glycemic index and even bigger bullshit I won't even remember. And then, somewhere in the night from Saturday to Sunday, he calls me slurring asking to give him a ride home because "he can't". A jock, fuck, a liquor jock.""",
410825: """xxx: Thanks to my iPhone I realized that my hands are always dirty and my ears and cheeks are greasy""",
410826: """A math textbook problem for 1st grade:
Petya and Misha have last names Belov and Chernov. What last name does each of the boys have, if Petya is 2 years older than Belov?
The child's dad: Is this still the subject of mathematics, or have you moved on to biology?""",
410827: """Riding in a taxi in the back seat. another passenger in front. The stereo plays senseless and merciless Russian chanson-blatnyak. The guy in front after a few minutes of silence says:
- Bro, maybe put on something less action-packed?""",
410828: """Mikage Souji: Bought a new phone.
Nevermind: android, windows, apple?
Mikage Souji: Pink!""",
410829: """xxx: how to enable the third core on a laptop, it's slow. The store told me it will turn on at home.""",
410830: """xxx: you won't believe what I downloaded 2 gigs of yesterday, watching all day )
ууу: what?
xxx: boys and girls, and also their parents, want to see funny stories? Does that ring a bell?
ууу: is it porn or something?""",
410831: """loderan: from documentation:
In case of a critical update error:
1. Determine the cause of the error
2. Eliminate the cause of the error
loderan: I even know who wrote this documentation""",
410832: """Bookstore, customer - a lady about thirty-five:

— Tell me, is Exupery's "The Little Prince" only in this edition? It's a gift, and the drawings are kind of childish...""",
410833: """xxx: Since May they raised the rent for tenants. Today I get a signed contract with a cover letter: "We return to you the real estate lease contract #215, signed and sealed with the organization's seal of sadness".
xxx: Almost cried... Now sitting thinking - maybe I should really lower their rent?""",
410834: """A northern friend gloomily flips channels and irritably turns off the TV:
- Fuck, what idiots? Three movies in a row. In three movies the heroes are where there's brutal frost, minus 40. Not one has a hat.""",
410835: """xxx: yesterday my place was a den of debauchery
yyy: got drunk and coded in Delphi?""",
410836: """1111: want a new Priora for 320?!
1111: it even has power steering!
2222: the steering wheel gets stronger and refuses to turn?""",
410837: """Hakuja.Baiashi: Life has battered me.
Darkness.Quatre: You battered yourself, and you should be grateful life didn't leave you.""",
410838: """ххх: today drove my first driving lesson
ххх: the instructor threw the keys, named the address and said, like Gagarin, "what are you sitting for?\"""",
410839: """Ххх: there are sorrows in life, right before sex we argue, and the woman in anger throws a new pack of condoms out the window, and kicks me out of the house. I walk the street at night thinking, what a setup, what's the probability anyway.....
Ххх: and then an unopened pack of condoms falls almost on my head. Another poor bastard =(""",
410840: """xxx: I have a friend, totally nuts. I run to him once looking for sympathy. I say, imagine, my dear almost strangled me with a mouse cord in a fit of jealousy.
xxx: He thought for two seconds and declares - I told you, should've bought a wireless one...""",
410841: """Text from a girlfriend, about two cats, verbatim: "Sema bites Fedya on the willy, Fedya is confused....\"""",
410842: """Шляпа:
Our CEO is a genius. I think he could sell a city, air, and Monday. Today he invoiced clients for two devices that we don't produce, they don't exist yet either in development or in production, there was only a thought to make such devices. No components, no schematics, no firmware, there's only an idea...

Шляпа:
And he SOLD them!!!!!""",
410843: """xxx: I have a plastic human eye here, I stick it in food. Such delight — if you peer into a marshmallow, it starts peering into you""",
410844: """mr.D: Yesterday bought wide jeans for summer. Grandma called them "sharovary". Remembered last summer... The balls really were getting roasted...""",
410845: """Лешка: the teacher said to make and bring birdhouses. what to do - on the weekend bought boards, made them. We come to kindergarten - I'm hauling the birdhouse on myself, it's heavy, the bastard
Лешка: there everyone's in shock - it turns out they need to DANCE with the birdhouses
Лешка: the dance of little starlings, damn )
Лешка: and not a single bitch told us about it!
Лешка: I felt like a dad who sewed a "quail" costume from internet jokes""",
410846: """in a medical university dorm
xxx: the dentist neighbor is totally overstudied.
xxx: comes today and says:"Just saw your classmate, she always says hi to me, and I don't even know her name. Like, you know, with the wrong bite\"
xxx: froze for a minute and says: "uh, I mean, a blonde with a C cup\".""",
410847: """xxx: They say lovers eat little and sleep badly.
xxx: I've been eating fuck-all for a week (stipend ran out) and writing a term paper for the third night.
xxx: Beloved. WHERE ARE YOU, AY!!!""",
410848: """News on the radio at work:
"...The Chernobyl exclusion zone is becoming less dangerous for human life. This is evidenced by the fact that wild boars, wolves and even lynxes are returning there..."

colleague: Moreover, it's one animal.""",
410849: """1: All you need is a screwdriver and 5 meters
of rope!

2: What for?

1: Well... Rope is the most useful thing for all occasions. And the screwdriver can be sold!""",
410850: """xxx: why would a group of virtual-sex lovers friend-request our military history club?
yyy: I'm sloooowly moving my troop contingent onto your territory...""",
410851: """nvs: Interesting iron you have!
ДНАА: *Proudly* Electric! China, end of the 20th century, the Dryan dynasty.""",
410852: """Klim73: How are the expanses of Suomi?
Pjrosenok: Vanya is an idiot, Vanya is a complete moron, I'll kill the bastard
Klim73: yeah that's clear, but how's Suomi
Pjrosenok: Vanya, filling out visa forms, in the "purpose of visit" field wrote sex-tourism, to Finland, fuck!!! No visa!!! we're going to the dacha to drink vodka with grief, come if you want""",
410853: """КотЭ: HURRAY!
КотЭ: I achieved it!
КотЭ: they started feeding me at work!
КотЭ: made a comp, get a cupcake.""",
410854: """On a forum about condoms. There's a discussion about which are better.

ххх: <марка> the glowing one tore in half)
ууу: were playing Jedi?""",
410855: """пельмешк: vitya wolf's head :D dreads long to shoulders. Not used to it at all.
данян: wolf's
данян: I thought you were writing a poem
данян: vitya wolf's head, dreads long to shoulders. Vitya likes to eat halva, he's smart and broad-shouldered""",
410856: """zzz: You reminded me how some 20 years ago we went to the clinic for the military enlistment medical exam. Since we didn't come to take it at the enlistment office on time.
So, Oleg went with me and Lyokha. He was wildly, indescribably, afraid of dentists. And approaching the dentist's office, he was already whiter than sour cream and flinched at every rustle. And the doctor, a granny of about 60, was quite the character. She, smiling slyly, says - "I won't do anything, just take a look..." Anyway, she looked, scribbled something in papers and sent us to the office opposite - the surgical one!
You should've seen Oleg... This creature, already resembling a ghost, seeing and hearing nothing around, doomedly floated into the surgical office. And there the surgeon - an uncle of considerable size, having looked at the dentist's papers, gave me and Lyokha one shot each, and Oleg two. Turns out he needed to remove 2 teeth on both sides.
So, we're sitting waiting for the shots to take effect, and the nurse asks Oleg "well? is it going numb?" He goes "uh-huh", she decided to clarify like from which side and says "what's going numb?", and he so innocently and sincerely - "my legs!\"""",
410857: """Ням!: Zhenechka
Женечка: Yep
Ням!: And who reinstalled your Windows?
Женечка: Damn, I don't remember anymore, what's up?
Ням!: Let's strain and think.
Ням!: the phrase "crooked-handed dick" who do you associate it with?""",
410858: """Welvimar: killer ad. "We want to work hard, but first we'll eat." And then we'll rest, and then eat again, look and the day is gone""",
410859: """она: I'm bored...
она: I probably know the Internet by heart
Он: Will you be my provider for 350 rubles a month?""",
410860: """xxx: you're so disobedient
yyy: does it turn you on, darling? )
xxx: it pisses me off""",
410861: """xxx: If summer is as hot as the previous one, the most relevant pickup line will be "Hi, I have air conditioning\"""",
410862: """xxx: she cooed and kissed my cat for so long, the eyes on his face expressed suffering and despair...
xxx: I tell her: "he just finished licking his ass, he's tired, hold off on the kisses, let him catch his breath\"
xxx: so she didn't even go brush her teeth, just left like that.""",
410863: """zendzirou: By means of a chocolate bar, 1 condom and a small amount of liquid soap (which is in the toilet) you can falsify an event that will make half the office look askance at each other.""",
410864: """xxx: the future has arrived.
xxx: a tablet computer got lost in stacks of papers...""",
410865: """xxx: Asked the secretary to drop the needed document on the shared network drive. To the question "where?" I automatically muttered "to the root" and forgot.
xxx: When I started looking - it's not there. Asked if she dropped it - "yes, to the root, like you asked"
xxx: And then I found a folder "Root" on the share...""",
410866: """TANATOS: after long programming "in the" turns into "int he\"""",
410867: """From a discussion about dog attacks:

_method_: in a stressful situation a person can do a lot
ynestulya: yes, I can shit my pants very fast!""",
410868: """ххх: Nothing works! Why?
ууу: For it to work, you need brains!
ххх: LIKE HELL!!
ууу: Exactly, but it should be brains.""",
410869: """Infant day at the clinic, among moms and dads with small kids in line sits a guy about 17.
One of the dads: Why'd you come on infant day? Still sucking the tit?
Guy: uh-huh, at the enlistment office =(""",
410870: """xxx: Was walking home calmly at night, a girl ahead.
xxx: Looked back and ran.
xxx: Barely got away from her.""",
410871: """Watched a scene at MediaMarkt: a guy stands at the counter with external hard drives, examining something. A pretty girl consultant approaches and asks if she can help with anything. The guy looks at her so sadly, sighs and says in a tragic voice: "No, you won't agree" and leaves""",
410872: """Remember my cousin told me, when he was in school:

Easter. Festive atmosphere at school. One body decided in advance to turn an egg on a milling machine, paint it and come to school with such a mega-bitok, to beat everyone that day. But he didn't suspect that he was NOT the only smart one. So he walks, walks, everyone's eggs around are already broken, and then comes the climax. These two imbeciles met, decided like, come on hit, crack... Such a loud sound of wood throughout the school. Curtain. Then they were carried away by an enraged crowd of deceived )""",
410873: """(Comment about the new main heroine of Transformers-3 on KG)

Damn, a scary woman (not about beauty).
She has the same expression in all photos.
Transformers are smashing the city - she thoughtfully gazes into the distance.
Witwicky flirts with her - she thoughtfully gazes into the distance.
A fucking huge robot flies overhead - she thoughtfully gazes into the distance.
She's like a bear. You can never tell from her face what's on her mind.""",
410874: """xxx: Often heard that you can't photograph sleeping children. Why can't you do this?
yyy: Why only children and only sleeping? Don't you know that you can't photograph people at all - you thereby steal people's souls, becoming a competitor of the devil. And he doesn't like competitors and will come for you soon.
zzz: You just need to use Hasselblad cameras, then he'll understand he's not a competitor, and will hang himself from envy.""",
410875: """Sh1yapnik: Coming out of a store, two men stand by a Lexus, in jackets, important, smoking... finished smoking, got into a seven (Lada) and left.""",
410876: """xxx:
It was in October last year, went to GUM to buy my beloved a laptop bag. Chose, carrying to checkout. The guy consultant, all sweaty (end of workday), hands the bag, the receipt and says:
-Good morning...
To which my beloved, absolutely automatically, reacts:
-Bon appetit!""",
410877: """xxx: is it normal that my husband calls me Snore��цель my love?""",
410878: """A worker's explanatory note, caught trying to steal non-ferrous metal:
"I heard a suspicious noise in the bushes and went to look. In the bushes stood a bag with copper. I thought a thief was nearby and deciding to stake him out sat down next to the bag. But the thief snuck up from behind and introduced himself - it turned out to be our chief engineer, who accused me of theft. And I'm not guilty.\"""",
}

def main():
    recs = {}
    with open(srcp, encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line:
                r = json.loads(line)
                recs[r['id']] = r['text']
    assert len(recs) == 1000, len(recs)
    for k in EN:
        assert k in recs, k
    cur = 0
    have = set()
    if os.path.exists(outp):
        with open(outp, encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line:
                    cur += 1
                    have.add(json.loads(line)['id'])
    with open(outp, 'a', encoding='utf-8') as f:
        n = 0
        for i in sorted(EN):
            if i in have:
                continue
            f.write(json.dumps({'id': i, 'ru': recs[i], 'en': EN[i]}, ensure_ascii=False) + '\n')
            n += 1
    print('batch28_8 OK, wrote', n, '(output now', cur + n, 'lines)')

if __name__ == '__main__':
    main()
