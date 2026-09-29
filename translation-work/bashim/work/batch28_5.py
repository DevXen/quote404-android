"""Chunk 28 batch 5: records 401-500 (ids 410479-410578). Appends to out/chunk_28.jsonl."""
import json, os

EN = {
410479: """xxx: The most atmospheric moment in Deus Ex was when I hid behind a column and overheard, on the other side of the column, one soldier saying to another "I don't like this place. It always feels like someone's sneaking up on you\"""",
410480: """xxx: damn I feel like an idiot
yyy: I feel that way about you too)""",
410481: """Found a whole website populated by glamour kitties... Forum topics:
Brain diet (key point)
girls, how long are your legs on the inner side?
Got a man - spin the man, no man - spin the hoop!
If you loudly farted in the office, what would you do next?
I used to be a hamster! I want my weight back!
how to properly greet a man from work?
are there girls who don't masturbate at all?
everyone-everyone! write your shoe size and waist measurement!
All men are bastards! The main thing is to find a non-drinking bastard with good potency!
rate my boobs! already saving money to lift and enlarge them! how urgently do I need the surgery?
which mia colucci is better, luisana or anahi?
Shoes. Beauty or the edge of madness.

And as the final chord: a schizophrenia test! how many points do you have?""",
410482: """xxx: my dad is schizophrenic, but he's good people""",
410483: """Another post about movie translations.

Mowgli_Dahab: I don't get how "Thursday" became bloody.
Nurkambay: Same way "Hitch" became "Hitch's Method: Pickup Rules". Pity the title is so short, I'd have added a couple more paragraphs and a little poem.""",
410484: """xxx: You're an admin. And your jokes are admin jokes :(""",
410485: """xxx: And why do they say cabbage makes your boobs bigger? Some microelements?
yyy: they do get bigger. take a head of cabbage, cut it in half, put it in your bra.""",
410486: """ah, programmers
xxx: want a bun?
yyy: no, I don't like mac
xxx: I don't like it either, I prefer sony vaio, but do you want a bun?""",
410487: """ххх: I love spring, it's so romantic!
ууу: yeah, there's a tractor floating around my yard! so romantic, like a swan on a pond...""",
410488: """Came to grandma's village, decided to throw a disco for the village friends. Got my rarity from the attic - a Soviet amp and Soviet "Radiotekhnika S90" speakers (for those who don't know, the size of a small fridge), hook them to the laptop and crank it up to check it doesn't wheeze at high volume. Stick in a flash drive with music, and then Kaspersky finds a virus on it.

In the evening the locals pestered grandma with requests to sell them lard.""",
410489: """xxx I mustn't fall in lo@#$*!ve with you
xxx always liked this word in chat )
yyy mustn't rela@#$*!x
zzz I won't use swear@#$*!words anymore
yyy why can't you use@#$*! such words?)
zzz The captain of this shi@#$*! said so - the moderator
xxx well damn
xxx for 3 ru@#$*!es you can settle this matter
xxx I'm not a captain, I'm a commander and my sa@#$*! is bared!
ttt holyshit! )
xxx uhh
ttt I thought the filter would work""",
410490: """Mr Улей: Sanya, can I ask an intimate question? Man to man :-)
МоркOFF: Go ahead
Mr Улей: Do you shave your armpits?
Mr Улей: And one more question
Mr Улей: Can the Christmas tree be taken out yet?""",
410491: """xxx: Why does TV call idiots and freebie hunters "trusting citizens"?""",
410492: """xxx: cat fell asleep on my mobile.
xxx: couldn't resist. wrote my buddy on ICQ: "Call me!!! URGENT!!!\"""",
410493: """xxx: Fish look for deeper water, and man - for Wi-Fi""",
410494: """xxx: Did you know storks can sleep 10-15 minutes during flights?
yyy: now it's clear how children who don't look like their parents appear )""",
410495: """xxx: Everything that doesn't kill us - later regrets it deeply.""",
410496: """xxx: Sat in line at the clinic to see the therapist.
xxx: Overheard an indignant discussion of two grannies about 70, about how qualitatively and promptly Doctor House treats and what terrible hackwork we have.""",
410497: """in comments to a photo with wrinkled fabric
ХХХ: bought the fabric, couldn't even iron it )
YYY: the only thing in this world you must iron is manuls. everything else is vanity of vanities and vexation of spirit.""",
410498: """xxx: Pashka is so pumped, is he an athlete?
yyy: Yeah, a forward of the South-Butovo team in mobile-phone push-ups.""",
410499: """xxx: honey, I decided to give you a gift - buy a car. Guess, starts with "O" plus 2 letters.
yyy: an Audi or something?""",
410500: """apoapo:
Guys, the correct answer to your girlfriend's question: "Doesn't it bother you that your friend's girlfriend is prettier than yours?" is not "No", but "What are you talking about, you're way prettier than her!"...
I screwed up here...""",
410501: """GreenScarf: Want to watch a good disaster movie, can you recommend something?
Крысько: watch the news.""",
410502: """karamelka_v:
For two weeks my husband went to work with an axe.
- What do you have today?
- Negotiations, - he says, and stuffs the axe into the briefcase with the laptop.""",
410503: """Кисса: Yesterday a traffic cop grabbed Pavlych by the ass at the traffic light.
Лёвчик: For what?
Кисса: For the ass.""",
410504: """xxx: do my coursework for me?
yyy: And the magic word?
xxx: sudo""",
410505: """Izabra: time 0:58. A sleepy mom walks by and declares: "stop sitting at the computer, go take a walk instead... oops... I mean go to sleep... yeah...\"""",
410506: """T-34:
parents came to visit, brought gifts. for the cat a cat house, such a cute one. and for me... and for me - a cat house for the cat.""",
410507: """unikate: my friend once had depression. she wanted to distract herself, went to buy some youth comedy and some kind soul sold her Requiem for a Dream.""",
410508: """XXX: Warn them that I'm actually in Tyumen. They're beyond the Urals, and I'm in Tyumen. And the nearest metro station is 337 kilometers from me. At the railway station of Yekaterinburg. Since there were precedents...""",
410509: """09/04/2011
xxx: damn, what to wear?!
yyy: a spacesuit. this doesn't look like our planet""",
410510: """xxx: ORANGE GLASS, ORANGE WALLS, ORANGE CHAIRS, ORANGE CARPET!
xxx: I'll never refill an inkjet printer again!""",
410511: """xxx: very soon I'll find the one.
xxx: the one who will forbid me to periodically and causelessly drink, smoke and swear.
ууу: Aren't you afraid that when such a person appears and starts forbidding you to drink, smoke and swear - you'll tell him to fuck off with a cigarette in your teeth and a beer can in your hand?""",
410512: """xxx: By the way, philosophy delivered today. They told us about talking apes.
xxx: They talked with gestures. Sign language. Turns out they learn this language very easily. Moreover: they start communicating with people quite well, build sentences, and can express the same thought differently depending on mood. Some even tried to name-call.
Then these apes were sent to zoos (the experiment is over). I cracked up imagining the reaction of deaf visitors to the zoo who approached the enclosure with such apes.""",
410513: """BuKoDuH: in the week I was sick I managed to pump up my abs while coughing... :3""",
410514: """xxx: Of course in 10 years in the roleplay movement I've seen all kinds of girls "with cockroaches". But this one!...
xxx: Feels like Chukovsky wrote his "Cockroach" based on her.""",
410515: """Co: only with the thaw did the true purpose of speed bumps finally become clear to me, watching how a line forms at them to cross, not swim across the road ^^""",
410516: """xxx: I had a son born!
Сергей: congratulations!!!!!!!!!!))))))) what did you name him?
ххх: Sergey )
Сергей: old.""",
410517: """On Sunday, April 10, Yekaterinburg law enforcement tried to stop a march dedicated to Soap Bubble Day.
"On April 10 by 14:00 people with soap bubbles started gathering at the Drama Theater. Militiamen were already on duty by the theater and announced through a loudspeaker that blowing bubbles is prohibited: this is an unauthorized event, there will be no soap bubble march either, everyone must disperse\"""",
410518: """ххх: so what's the problem?
yyy: the problem is that I want to be the father of her children, but for her I'm just "that white priest in Ragnarok\"""",
410519: """x: hi, my little fox
y: Are you saying I'm an animal?!
x: no, you're a mushroom""",
410520: """xxx: I made crab salad, go eat
ххх: just add mayo, the rice absorbed it
ххх: and add salt, the rice was unsalted, it absorbed the salt too )
ууу: I'd better go eat fast, before the rice absorbs the crab sticks and corn""",
410521: """yesterday a conversation with my wife:
Ж> don't you know, is <name> a good doctor?
Я> yeah sort of, what, are you sick
Ж> no, I was thinking of removing my lower ribs so my waist is thinner
Я> that's not for him, that's for Goodwin
Ж> does he also work at our hospital? is he a good doctor?
Я> no, he's not local, works in another city, in the emerald one. Don't know how good he is, but he definitely helped someone with brains.

So why am I a jerk after this?""",
410522: """Sitting with classmates, discussing employment probability after graduating Uni.

ааа: They won't take you straight to a normal job.
ббб: Yeah, I've seen their requirements: "Higher education, attractive appearance, fluent English and German." And that's for a COURIER vacancy!!!
ааа: Idiocy!
ввв: Well depends on the courier, maybe a drug courier.""",
410523: """xxx: Will you invite me when you're getting married?
yyy: I'd invite but there's no one to marry
yyy: I just want to come home, and instead of a hungry dog or cat, my beloved greets me )
yyy: and from behind the computer she yells "hey, did you bring food? go cook, me and the dog and cat got hungry here" :D""",
410524: """xxx: let's hope people are getting smarter
yyy: what makes you think that?
xxx: remember - there used to be cockroaches, then they suddenly disappeared somewhere, after which the mass emo-goth-glamour era began...
xxx: and now they're starting to appear again, maybe they got tired of sitting in these people's heads and they'll return back HOME!!! :)""",
410525: """xxx: Today I came to the conclusion that on weekdays my cat demotivates me. By the time I leave home for work, he manages to have breakfast twice and sleeps on my couch upside down, paying no attention to external stimuli at all. Thereby he doesn't just question the principle: "Who doesn't work - doesn't eat", but completely refutes it. ((((""",
410526: """xxx: recently was browsing a forum, there was a discussion about inheritance of eye color.
xxx: well as a person studying biology it interested me, decided to read what they write
xxx: there one person asked what children would come from such-and-such parents, and there one sensible guy said that two blue-eyed parents will never have a brown-eyed child, two recessive traits, blah blah, whatever.
xxx: and then such a post: my parents are both brown-eyed, and I'm blue-eyed. can that really not happen?..
xxx: That was the last post in that thread. Nobody dared to destroy the poor guy's worldview""",
410527: """stud21: I've been living on broths for a week
aftor: which ones?
stud21: pelmeni...""",
410528: """Recently was in the hospital.
After the first sleepless night the thought appeared that by analogy with "places for non-smokers" "wards for non-snorers" would be relevant""",
410529: """NikitA: I definitely lost weight
NikitA: only the scale doesn't show it""",
410530: """sms from husband:
Sitting in the insurance company, a guy here while adjusting his shirt dropped a gun. Now I'm thinking, maybe this is his argument for the insurers?""",
410531: """Today I was buying banned-from-sale 150W incandescent bulbs under the counter at the bazaar. The seller and I felt like a drug addict and a drug dealer.""",
410532: """Машук: do you know where my phone catches signal in my room? Found this point X
Машук: in the closet.
Белка: that's a phone booth, not a closet )
Белка: look for a door, maybe there's Narnia in there too""",
410533: """Космонавт: on Vyborgsky there's a banner hanging with no company name or initials, just the phrase :"buy an apartment in the city"

so want to add at the end "be a man dammit" :))""",
410534: """Just now on TV3 they showed a movie about man-eating monkeys, now they're showing one about man-eating tigers. Feels like only the lazy don't eat people these days.""",
410535: """XXX: we had a vending machine in our building selling all kinds of chips and chocolate bars, so our hackers from the programmer-friendly department tested it for vulnerabilities, and found a bug )
XXX: the chip mechanism wasn't great there, if you bought 3 chocolate bars, they'd knock the chips lying below when falling, and one or two packs would drop as a bonus to the chocolate bars )""",
410536: """From LJ:
nayen: Don't remember where I read that vampires have a feud with the domovoi tribe, that's why they can't enter a house.
lanss: Nafanya vs Dracula!""",
410537: """News: In Petersburg a truck with a driver and 20 tons of peanuts was stolen

One of the first comments: Damn squirrels - totally out of hand....""",
410538: """xxx: Walking home from school.
xxx: Enter the entrance and hear someone talking, but can't figure out where. Well whatever, screw it.
xxx: Call the elevator, doors open and two girls are staring at me from the elevator, one says to the other: "Damn, we didn't press our floor\"""",
410539: """answers from mail:

1: Help choose a player in l2. Pumped a dark elf SH so far. Basically satisfied - only cast 847 with tattoo and scrolls. Maybe there are other good profs? Advise a newbie.. PREFERABLY A MAGE) And another question: why when I pumped a sub SS on a human, its magic attack is lower than on a light elf SS?

2: Better to pump a CEO of a successful company, or at worst a department head. A friend recently pumped a middle manager, basically really likes it. Going forward everyone recommends pumping subclasses like "excellent husband", "good father", "wonderful friend". And spellsinger, that's just... meh..""",
410540: """xxx: want a cheesecake class
yyy: maybe an object?
xxx: no
xxx: a class and make a bunch of objects
xxx: and the destructor is already ready ...""",
410541: """tertuykalach: At 15:00 technical break, can drink tea. SUDDENLY the general walks in, like: "where's your calendar here?". And I have half an eclair in my mouth... For once the CEO addressed me, and I, fuck, it's like I haven't eaten eclairs in a hundred years.
Витек: *ROFL*
tertuykalach: there was an option to moo "you're late, sir, I'm finishing off November", but I didn't risk it...""",
410542: """From a car forum, choosing a first car:

Here they tell me the first car should be a Russian one. Smash it to smithereens through inexperience, have plenty of fun with repairs. Then buy a good foreign car and, shedding tears of happiness, feel like you're in a spaceship of the distant future :)""",
410543: """xxx: as a kid I thought Gorbachev was president because he was the only one in all of Russia with a birthmark on his forehead shaped like the map of Russia""",
410544: """xxx: The ad slogan "Let's breathe life into every log!" for some reason I associate with the sex act :-)""",
410545: """xxx: and where does the doctor put the thermometer?
yyy: depends who you're playing doctor with""",
410546: """xxx: An old man cast his net into the sea and caught a fish that spoke to him in a human voice: - Let me go, Old Man!
xxx: and the old man realized the Japanese weren't telling something about Fukushima...""",
410547: """yermax: hi)
yermax: you here?
yermax: helloo!!!
yermax: You've got the silent treatment. It's autoimmune.""",
410548: """Вика the marketer: When the antivirus is running, I can't work. I propose the admin allocate me a second computer, so the antivirus works on one, and I work on the other... he's still thinking...""",
410549: """xxx: What are you wearing now?
yyy: Business, fuck...
xxx: I'm slowly removing all your obligations...""",
410550: """xxx: My wife's salary (a first-grade homeroom teacher) is 4500 r.
xxx: Unemployment benefit is 4900 r.
xxx: Education is priceless, because it has no price.""",
410551: """akill: I feel like a bird from Angry Birds. I want to kill you, you pig.""",
410552: """Она: would you like to try a threesome?
Он: Damn, I'd like to try a twosome! Tired of doing it alone...""",
410553: """xxx: They started fighting foul language in our city again
yyy: need to come up with some punchy name
yyy: for example, "Whirlwind-antidick\"""",
410554: """Comment on the movie "Alien Invasion: Battle for Los Angeles".
xxx: The Americans in the movie act like rebels, the aliens brought them democracy, and they....""",
410555: """xxx: Rambler News: "Fursenko banned suspecting politicians of idiocy"
xxx: I don't even know how to react anymore...""",
410556: """Conversation with a girl at work:
xxx: want a candy?
yyy: you're bugging me with your candies, why don't you bring sausage?!
xxx(crying): nobody gives me sausage, I'm a giiiirl""",
410557: """xxx: I really need a job...
yyy: Why?
xxx: What do you mean why? What will I live on?
yyy: Hunting and fishing!
xxx: I'm against killing animals...
yyy: Then eat them alive!""",
410558: """lizi: I work in a photo studio. A strange client often drops by, as she says, a teacher at some school...

- Tanechka, photograph me right away.
- I'm not Tanya.
- Wow, I like your blouse, only if it weren't for this and that. Right, Tanya?
- I'm Liza.
- Did you work yesterday or not?
- I did.
- Well then you're Tanya!

lizi: I really feel sorry for her students...""",
410559: """svipride: They give us our salary in little envelopes
svipride: and they ask for the little envelopes baaack!""",
410560: """ххх: All guys think they're stronger than girls, but for each one there'll be one who'll make him suffer with one look (c)

ууу: Stronger or not, debatable, but I broke my guy's rib! He was getting on my nerves!""",
410561: """xxx:
An acquaintance told me.

xxx:
An old lady came to their bank to talk about a loan.
They tell her:
-Well sit down, let's discuss loan options and repayment terms.
She jumped up in horror:
-What? IT HAS TO BE REPAID?""",
410562: """xxx: what's this mixer.exe process?
xxx: what other household appliance is eating my CPU?""",
410563: """From LJ:
Yesterday a chicken tricked me too. I put it on to boil, and it managed to both boil and fry.
Trouble with these birds.""",
410564: """xxx: More and more it seems that this year May beetles will arrive in down jackets""",
410565: """- Yesterday I went to bed early.
- Yeah? What time?
- Doesn't matter. I went to bed YESTERDAY!""",
410566: """xxx: On April 12, 1961 thousands of Moscow residents took to the streets, rejoicing at the first human space flight. Fifty years later the children of those Muscovites decided to celebrate this joyful holiday by walking the streets of their city in spacesuits, holding Gagarin's photos. Not so fast... Result of the festive art march "Cosmofest": seven people detained, awaiting trial.
yyy: xxx, what, are you for permissiveness? Want people to rejoice without approval? There must be order!
zzz: Can't disagree! Rejoicing? Be kind enough to file an application with the prefecture!""",
410567: """My daughter is 7, recently looked at old photos. Group photo. We explain to her: "This is mom, this is grandma...", but the kid is interested in something else. Points in amazement: "And what's that?" I answer: "A carpet." She falls silent, stunned, then blurts: "What, are you all lying on the floor???\"""",
410568: """Santos: As our PE department head used to say, "You will gnaw the granite of science. And if you don't gnaw, I'll knock your teeth out and you'll suck it\"""",
410569: """A letter arrived:
JSC "Sberbank of Russia" - Central Russian Bank hereby notifies you that following the results of 2010, in connection with taxable income in the form of material benefit from savings on interest when using credit funds in the amount of 10.42 rubles, you have a debt on personal income tax in the amount of 4.00 rubles.

I call the bank. They answer, well, the interest on loans dropped a bit - you got a benefit - 10 rubles. And benefit is taxed. So the tax must be paid.
To pay the tax, I need to:
1. Go to Sberbank, get a certificate.
2. Go to the tax office, fill out a declaration, get details.
3. Go to Sberbank to pay.
AND ALL THIS BECAUSE OF FOUR RUBLES!!!!! Love my country.""",
410570: """xxx: I think I dislocated our secretary's brain by asking whether the inventory book is entered into itself.""",
410571: """ххх: Bought a guitar and decided whenever I want sex, to learn to play it.
xxx: now I have calluses on my left hand.""",
410572: """xxx:
TV announcers have been delighting me lately. In the last month they changed the emotional coloring of their voice so much, in moments when they talk about the situation in Japan! The first week in their voices you could hear "AAAAH!!! PANIC!!! WE'RE ALL GOING TO DIE FROM RADIATION, IT EXCEEDS THE ALLOWED DOSE BY 1.5 TIMES!!!", and now they already say in such a casual voice "well today something exploded there again (yawns), the situation was assigned the highest danger level... (yawns again and in a brisker voice) And now to sports news.""",
410573: """xxx: Couldn't you have said that she's sitting behind, at the next table and hears everything?! Kick me under the table at least?
yyy: Wasn't it enough that I widened my eyes and broke out in a sweat?""",
410574: """online game chat

Blonde: are you a girl?
AGATUSHKA: a guy
Blonde: why is your nick Agatushka?
AGATUSHKA: I love Agatha Christie )
Blonde: is that your girlfriend or something??""",
410575: """ХХХ: someone teach my boyfriend not to tell the truth, otherwise he'll be without sex and without dinner forever! I'm out of patience!""",
410576: """Insaider: ads that lie to people are evil, I hate them
Insaider: just now there was one about mortgages on the radio
Insaider: kids laughing in the background, birds chirping, the announcer cheerful
Evil: and how should it be?
Evil: KHSH KHSH KHSH... guillotine on the neck, all this to children crying, chains rattling, bells in the background and such a scream - MOOOORTGAAAAGEEE!!!!!""",
410577: """xxx: should I retell her the inhabited island... although I haven't read it
yyy: it's fine, Bondarchuk managed, and you can too""",
410578: """xxx: In Russia the longest-living people are those who don't believe in the green traffic light, in government promises, in free medicine and in the selflessness of humanity.""",
}

def main():
    src = '/home/hatch/workspace/translation-work/bashim/chunks/chunk_28.jsonl'
    outp = '/home/hatch/workspace/translation-work/bashim/out/chunk_28.jsonl'
    recs = {}
    with open(src, encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line:
                r = json.loads(line)
                recs[r['id']] = r['text']
    assert len(recs) == 1000
    for i in EN:
        assert i in recs, i
    cur = 0
    if os.path.exists(outp):
        with open(outp, encoding='utf-8') as f:
            cur = sum(1 for line in f if line.strip())
    with open(outp, 'a', encoding='utf-8') as f:
        n = 0
        for i in sorted(EN):
            f.write(json.dumps({'id': i, 'ru': recs[i], 'en': EN[i]}, ensure_ascii=False) + '\n')
            n += 1
    print('batch28_5 OK, wrote', n, '(output now', cur + n, 'lines)')

if __name__ == '__main__':
    main()
