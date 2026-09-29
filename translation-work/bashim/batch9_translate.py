#!/usr/bin/env python3
"""Translate bashim chunk_13 batch 9 (input lines 901-1000). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
395089: """MGM:

Folks, who knows depressing places in Kharkov and surroundings? Interested in uninhabited old houses, destroyed bridges, abandoned depots, airfields, construction sites... 
In short, everything that reminds of Earth after the apocalypse. 

Share the info if you have it and don't mind. Ideally - throw in some kind of map

ABB:

I can give you the phone of a demolition-expert friend, you pick the place yourselves""",
395090: """first seminar of the first year in geodesy, studying one tricky apparatus
teacher: so, how will we establish the balance of the level?
student: well behind that thingy there are balance screws, you turn them and establish it...
teacher: well everything's correct young man, except for one word, not balance screws, but level screws""",
395091: """Prologue: I was afraid that during the thesis defense my laptop would connect to the network, pop up in ICQ by itself and someone would write something like "hi, ugly" - the message would pop up in the corner of the screen, the committee would freak out. so anyway.

 UGFS Master (12:10:04 21/02/2008)
waaait for iiiit giiive giiive
 Yuri Sergeevich (16:36:21 21/02/2008)
fuck, today I got exactly what you were afraid of
 UGFS Master (16:36:35 21/02/2008)
:)
 UGFS Master (16:36:39 21/02/2008)
at the presentation?
 Yuri Sergeevich (16:36:42 21/02/2008)
yes!
 UGFS Master (16:37:12 21/02/2008)
someone wrote "hi, shit-fucking whoreater"?
 Yuri Sergeevich (16:37:27 21/02/2008)
no, someone wrote: waaait for iiiit giiive giiive""",
395092: """Irusechka (12:49:01 21/02/2008)
li-i-i-isten

 Irusechka (12:49:22 21/02/2008)
me and Mishka and the little hippo have little balls in our butts! )

 Aliense (13:53:40 21/02/2008)
you and Mishka and the little hippo? O_o""",
395093: """xxx: Lineage has tits
yyy: but in WoW you can jump!""",
395094: """Hellgirl
what's the difference between tits and a bust?

 Semyon 
well first of all, there's one bust. and two tits 

 Semyon 
that's why tits are of course better than a bust""",
395096: """1: What do you think needs to be shown to attract people to the elections? 
2: Emmm... An exploding helicopter?""",
395097: """M: So what did you celebrate - surely not February 23rd?
W: yeah. I gave the men flowers and candy hoping that on March 8th they'll give me booze and condoms""",
395098: """Our first president's initials were EBN, which matched what was happening in the country. The second one's - VVP, well the gross domestic product sort of grew) The next one's initials are MDA... what the fuck could that mean)""",
395099: """komrad 
DON'T TYPE IN TRANSLIT (SORRY FOR CAPS)

 argu 
NE KAPSI (SORRI ZA TRANSLIT)""",
395100: """An amazing sight: 5 healthy guys trying to pretend they can't remember the name of the fish from Disney's "Little Mermaid". And you can see on each face that he remembers, but doesn't want to admit it""",
395101: """today I decided to buy myself some paper reading. went into a bookstore. walk up to the saleswoman (S), ask politely:
Me - Do you have "The Hitchhiker's Guide to the Galaxy"? 
S - Look over there...
Me - What's that?
S - What do you mean? Atlases, travel guides, guidebooks, maps.
Me - ...I need "The Hitchhiker's Guide to the GALAXY"
S - well yeah...
Me - yeah
S - hitchhiker's
Me - hitchhiker's
S - guide to the galaxy
Me - guide to the GALAXY!!!! Have you ever traveled the galaxy? 
S - well here's a star atlas
Me - Jeez... It's a book. Sci-fi. Douglas Adams wrote it.
S - aaaaah, that's in the other hall.
Crawling to the other hall. There I run into another saleswoman (S2).
Me - Miss, do you have "The Hitchhiker's Guide to the Galaxy"?
S2 - Atlases and travel guides are in the next hall.""",
395102: """<sav> sleeping 4 hours a day is hellish narcotics
<sav> I was afraid to blink at work
<sav> at 17 I blinked wrong - blinked myself out only at 18""",
395103: """1: if a fork falls - a woman will come
1: if a knife - a man
2: and if a grenade, then pizdets.""",
395105: """1. I am code that walks by itself
2. Code that walks by itself is a virus.""",
395106: """<jonie> spent three hours cracking a program, obfuscation, packing, anti-debugging... busted my ass, but cracked it
<jonie> wrote an addon for it in 30 minutes, looks nice, thought I'd send it to the developers.. sent it
<jonie> so they wrote back like "cool, thanks, here's a personal key for you"
<jonie> and where's the justice?!""",
395107: """DMN: (with regret)  I'm a stranger at this celebration of life...
ZBR: AND I'M A PREDATOR!!!""",
395108: """- Why are you so angry?
 - The bracket doesn't work...""",
395109: """led:

And "roles from packages" - please, no. As if we needed packages "administering the system" and granting permissions to whole groups, subgroups and gangs.

dottedmag:

I can just picture the crime news: "A gang of ruby packages illegally seized /usr/lib/python and demands privilege escalation and the release of postfix from chroot, promising to deinstall one python module a day until the demands are met\".""",
395111: """xxx: how's it going?
yyy: like a cosmonaut's
xxx: what's that like?
yyy: chicks love me, but the tastiest food in my place is toothpaste in a tube""",
395112: """xxx: We're walking after the concert, trying to cross the road, and not a single bastard stops of course. And Anya has a drumstick in her hands. So, without thinking twice she raises it, the very first car brakes, and our whole crowd rushed across
yyy: holy shit)
xxx: I think the traffic cop driving behind us was the most shocked of all""",
395113: """Steel: The inscription on the anti-smoking insert in a Chesterfield pack: Cigarette smoke contains thousands of chemical elements. Apparently nuclear physicists all over the world (and especially in Dubna) are banging their heads over synthesizing some pathetic 118th one for nothing! :)""",
395114: """kolakot:)what I like about Morrowind - you fight through a fuckton of monsters, reach the end of the dungeon, bypass all the traps, kill the most evil mob, crack the chest with the hardest lock, break all your lockpicks on it, and inside.. A SHOVEL!""",
395115: """MK: Today we were buying dumplings. Walked up to the checkout.
MK: The cashier, a young girl, swipes over the barcode reader once, twice... nothing
MK: Yells to the neighboring cashier: "Mariin, how do I find these dumplings?"
MK: She answers "enter for both cheeks"
MK. The cashier freaks out and stares blankly at the coworker.
MK: A minute pause.
MK: The neighboring cashier starts laughing, and through the laughter: "No, Lenchik, that's what the dumplings are called, 'For Both Cheeks'\"""",
395117: """maXi: bought a video card from Asus today

Bubble: how is it?

maXi: fine, only the box was alarming. among other things it says "Keep an eye on your home at all times no matter where you are blah-blah-blah". Right below it a translation like "Leave one eye at home, and don't worry wherever you are...". thought for a long time before plugging it into the motherboard...""",
395118: """Yu.M.: intelligentsia, it either exists or not, that's a fact!
Lyokha: by the way, Edik...
Yu.M.: Yes, by the way, Edik is a true intellectual!!!
Lyokha: * pensive facial expression *
well anyway, so Edik went off to steal benzene...""",
395119: """**BARS88**: I last played it like 15 years ago... but I still remember all the secrets and tricks
 Bovar: ha)) there are things that are hard to forget - like riding a bike - hitting from the right - opening a beer - and playing Mario""",
395120: """logos_:   A neighbor woman sits on a garden bed, rain dripping from above, complaining to herself: "Too bad there's no slavery, otherwise I'd be sitting on the garden bed, and a slave would be holding an umbrella over me\"...""",
395121: """1: for lunch a mug of broth and 400 g of beef tenderloin. For dinner plain lentils or rice - 100 g. That's how we live.
2: And what about breakfast?
3: And for breakfast an omelet with bacon from five eggs, a chicken leg, potatoes fried in lard, three sandwiches with boiled sausage, five sandwiches with smoked sausage, six jam toasts, four eclairs, and a liter of tea with fifteen lumps of sugar. :D""",
395122: """Svetlana (10:24 PM) : 
Hi.Wanna chat?I'm Dasha...
kamaz (10:28 PM) : 
Hi. Let's chat. I'm a Zhiguli)""",
395123: """Her: I love you!
Him: thanks!
Her: uh... Honestly, I expected a different answer..
Him: thanks a lot?!""",
395124: """<Брeзeнт> April Fools' Day this year falls on March 2nd""",
395126: """Puma (17:36:03 26/02/2008)
When you want to cry - call me, I don't promise to make you laugh, but I can cry with you, if one day you want to run away - call me, I don't promise to talk you into staying, but I can run away with you, if you don't want to hear anyone at all - call me, I'll sit very quietly, but if one day you call me and I don't respond - hurry to me, most likely I really need you..... 

Vincent (17:36:20 26/02/2008)
and if I want to get laid?
 
Puma (17:37:23 26/02/2008)
you heartless bastard...... you have no romance in you.....
*if you want to get laid - call me, I don't promise to put out, but I can want to get laid together with you""",
395127: """reread a support ticket with distrust: 
"the client isn't pinging the whores\"""",
395128: """Her: I'll definitely give you a lighter
Me:   ???
Her: do you usually light up from the gas stove?
Me:   well yeah, and how did you figure it out???
Her: when you came to my place drunk yesterday at 2 am, you tried 3 times to light up from the cold water of my tap and fell asleep on the kitchen nook...
Her: will a zippo do?""",
395129: """xxx what's the attendance like?
yyy 2k/14k a day, 36k/430k a month
xxx what does that mean? 2k/14k?
yyy hosts/hits
xxx what? I get hosts. and hits?
xxx what hits? which hits?
xxx striped hit. humpback hit. killer hit. hit mustache. save the hits! hit blubber...
xxx a trendy profession - hit-hunter.
xxx Moby Dick - the white hit.
xxx one hit can feed ten Aleut families for a whole month...
yyy fuck, you're a junkie""",
395130: """zealot: sorry, going to grab a bite
ReiClone: you should eat at the comp - it's good manners %)""",
395131: """Laying out the "Yellow Pages". The proofreader, sobbing with laughter, brings a printed chunk with the vet clinics:
 "...euthanasia and disposal, 30% discount on services for pensioners.\"""",
395132: """xxx: What are you up to?
yyy: well, the molar work has started
xxx: Avogadro renovation? =)""",
395133: """xxx: seen the ad "new fragrance from Diesel"?
yyy: yeah
xxx: when I heard it on TV I immediately pictured this homespun tractor driver in a quilted jacket with a 24mm wrench, face in oil, a 22-tooth grin and a voiceover "new fragrance from Diesel"
yyy: all the local milkmaids are yours""",
395134: """lily_loly_pop: if the laptop falls into the bathtub - will it kill me?

Seryozhka tekh: if not, I'll finish you off""",
395135: """I work at a printing house. 

Spent half an hour in a panic looking for the customer's phone number, to tell him his business cards are printed and he can pick them up...

Time for a vacation...""",
395136: """xxx: So, are you ready for the concert?
yyy: Fuck, the concert got fucked!
xxx: What is it this time?!?!?!?!?!
yyy: Vova was sitting rehearsing, strumming away, then Anka walks into his room, scissors in her hand. Then the music suddenly stops, 5 seconds of silence, and Vovan's piercing scream: "You mudafucking animal!!! This cable costs more than the guitar!!!" Then a hit of something hard on something soft, Anka runs out in a panic, a halved guitar flies out after her, Vovan runs out with the amp and yells "Come back bitch, I'm not done yet!"
yyy: As a result two guitars killed, a cable worth 15 grand, the amp, and I nearly died laughing :)))))""",
395137: """Malikova: And anyway we have a holiday coming soon! what will you give me?!
DX: And what did you give me for the 23rd?
Malikova: I gave my love and care!
DX: Then I won't give you a fucking thing""",
395138: """An excerpt from a memo by a 62-year-old 1C programmer about the admins:

"The main drawback of the SQL program is that the databases are stored in such a format that the files can't be viewed, since they significantly exceed the capabilities of opening them in other programs, for example, Eksel, and accordingly fixing them is associated with significant difficulties...\"""",
395139: """The best Zhirinovsky-for-president campaigning I've ever heard on the street:

"Zhirik for Prez!\"""",
395140: """from a debate (final act).
teacher (in despair): what do you think with?!!
female student (proudly): with what my mother bore me, that's what I think with!""",
395141: """So these churek chicks come in - they say - you sold us a charger - and our phone broke from it... How I laughed! anyway these dumbasses thought that the little box on the end of the charger charges slowly, and tearing off the box, plugged two ends into the socket... well and the phone got fucked - just scraps in a bag from the L6... it's clinical.. :-D""",
395142: """Two days in code and you type faster than you talk...""",
395143: """Passing a lab. One is passing, ten people are watching. The passer finishes answering another question. The teacher, addressing the watchers:
   - Does anyone want to comment on the answer?
   A voice from the group of watchers:
   -...first, fuck off.""",
395145: """Timai: And it's strange of course, yeah, sitting at 4 am in front of the comp, staring at the download manager and placing bets - which download stream will finish faster.""",
395146: """<Multashka> parseo: Got no one to drink with?
<parseo> Multashka: are you asking for company? I haven't gotten little girls drunk yet 
<Multashka> parseo: We'll see who gets whom drunk.)
<parseo> well then even more so 
<parseo> you'll get me drunk, take advantage of me, and then I'll get locked up 
<Multashka> parseo: I'm not like that.) And I'm not little, I'm just 14 years old.)
<parseo> Multashka: well for your 14 they'll give me 14 later, I'll get out a gray old man 
<Multashka> parseo: In our country nobody gets locked up for that anymore, and especially not for 14-year-olds.
<Multashka> parseo: I know about locked up or not, because my uncle works at the prosecutor's office.)
* parseo crossed himself 
<Multashka> parseo: Where are you? Why so quiet?""",
395147: """Art Cool (17:49:58 4/12/2007)
yeah, now driving... want me to come pick you up?

afanasa (17:53:21 4/12/2007)
no thanks, I still want to see my grandkids

Art Cool (17:53:38 4/12/2007)
so what about me??

afanasa (17:53:49 4/12/2007)
and you're offering to look at a great-grandpa""",
395148: """Today I asked my admin friends a riddle: A brother and a brother live across the path from each other, but never see each other. Everyone agreed it's IDE and SATA.""",
395149: """Damn, they say everyone's poor... Today in the cafeteria I saw two dudes chip in for one cupcake and cut it for two with a Visa Gold card!""",
395150: """Corbina are bastards... They messed something up and now it turns out I'm sitting not in Russia, but who knows where. Contra won't launch.
By the way... Contra in Russia is published by "Buka"

So I'm calling Corbina, going "what the fuck???!!!"
They tell me - we're doing everything possible. and you can also contact Buka.
I couldn't take it and answered - I'm already an adult and I don't believe in Buka anymore...

Wild laughter on both ends""",
395151: """Zaiko: on TV this thing, I'm watching about bubble wrap, the one everyone likes to pop. now I know the whole manufacturing technology.
Archie: will you make me one?
Zaiko: with that train of thought I'm not telling you what I watch anymore))""",
395152: """1: Have you ever seen Mongols?
2: Mongols?
1: Yeah.
2: Mongols?!! Dude, Mongolia is the most sparsely populated part of the planet. There's one and a half people per square kilometer! Can you imagine? That means somewhere there are tons of them, and somewhere there are none at all! Some Mongols there have never seen Mongols!""",
395153: """kiddy: you're harsh, Tatyana Maratovna...
Irresistible: huh?
kiddy: putting a screenshot of the "Image Viewer" with this picture into the archive instead of the picture..""",
395154: """Him: How do you manage to type so fast from your cell?! Or do you have a communicator?
Her: No, I just have lots of experience :)
Him: You can tell there's music school behind you! :)
Him: I'll set you as an example for my son now: "Once you learn to play the piano like the lady - you'll be able to chat on ICQ from your cell!!\"""",
395155: """1: Fuck, overnight - 284 emails and 282 spam. How good it was in 99.
2: Which year are you talking about?
1. O_O,1999
2. Young man, read the Golden Calf. "Lokhankin sighed joyfully, pulled out from under the wardrobe the 1899 "Rodina" in a sea-wave-colored binding with foam and splashes, looked at the pictures of the Anglo-Boer war, the ad of an unknown lady, titled: "This is how I enlarged my bust by six inches" -- and other interesting thingies.\"""",
395156: """Had an interview, they gave me a questionnaire, said fill it out.
There was a question: What would you not want to do?
I honestly answered: Blackmail, Murder, Extortion (I really don't want to do these) as a result they didn't hire me :(
Was it an admin they needed or something else...""",
395157: """Svolo4: my fucking Persian cat made a fucking Persian Gulf on my fucking Persian carpet!""",
395158: """Kim-Kong wrote
February 28, 2008 at 22:47 
Frankly, I was quite surprised when on mail.ru I saw a letter titled "Soul-Chilling Pizdets commented on your photo...\"""",
395160: """xxx: Got a flash drive?

yyy: yeah

xxx: then I'll stop by for five minutes, if you pour onto it video fragments of diploid zygote formation

yyy: huh?

xxx: porn""",
395161: """neon: now to kick the printer into working...
Izida: well what) plugged it in, put the disk in - works
neon: I have Linux, baby =)
Izida: baby) well, I like it)
Izida: it conjures the image of a tough burned businessman or gangster and a fragile blonde
Izida: and he's like: "I have Linux, baby"
Izida: and she's like "aaah" and falls)""",
395162: """111 (19:46) :
nothing to do

222 (19:47) :
accept Islam

111 (19:48) :
okay
how much does it weigh?""",
395163: """xxx: listen, please bring cigarettes - the nicotine hunger is killing me...
yyy: sure, just answer 2 (two) questions: Who are you, and where are you?
xxx: in the kitchen.
yyy: o_0 who are you???""",
395164: """Dr.Flex:
what are you doing? 
Raven:
a minute 
Dr.Flex:
collecting it from seconds?""",
395165: """~Be$t~: guys how old are you?
Pavlinus: I'll be 25, but for now I'm 11)""",
395166: """A:
What would you do, hearing one long beep and two short ones?

B:
???

A:
"One long and two short beeps usually mean the motherboard BIOS can't initialize the video card" 

A:
Less often they mean the "nuclear alarm" signal.""",
395167: """yyyyyy: In America, if you sell a lot of Mary Kay products, they give you a pink Cadillac for work.

xxxxxx: And here in Russia as a reward they give a Ford Mondeo, also pink :)

cccccc: And in Tajikistan - a pink donkey with silver ears. Looks gorgeous....""",
395168: """xxx: What does a beaver eat?
yyy: Bought a beaver? you'll find out soon...""",
395169: """Sasha: A new developer is coming soon and I'll be dumping this report on him.
Katya: You don't want to work with me anymore?
Sasha: I want to work with you, I don't want to work with this report.
Sasha: And you know why?
Katya: Why?
Sasha: Because I just can't shake the feeling that in the Me-You-Report triangle for the last 2 years I've been fucking the wrong one.""",
395170: """<Ponchik> Just went to get printer ink, I look - a pizzeria, the sign says "tasty pizza, pizzeria, pizza, pizza house". I'm thinking to myself - meta keywords!""",
395171: """Programmer:
Good morning Dmitry!
  Admin:
What did you break?""",
395172: """Vova Imengulov
I want the "Farewell Youth" tariff, 100 Mbit/sec unmetered. For 100 rubles a month.""",
395173: """Russian fantasy is just like Russian porn. senseless and merciless""",
395174: """o_dima: bananas need to be washed, because negroes in Africa touched them with dirty hands.
herrmuller: vividly pictured: a banana factory in Africa, a conveyor. a negro sits with the dirtiest hands and with Homeric laughter unceremoniously touches all the bananas.""",
395175: """What do you call a country where money is made to fit the width of an envelope?""",
395176: """At the institute they give sentence translation into an eastern language.
Here's what the Hebrew group had to translate:

...- Here are thirteen photographs from Israel.
- Very beautiful. Is that a tractor?
- No, that's my girlfriend.
- You have a very beautiful girlfriend. And is she in this photograph?
- No, in this photograph there are twenty-five tractors of our kibbutz.

KTct""",
395177: """1C is a combination of the drawbacks of object-oriented and functional programming languages""",
395178: """N. - why are you at work so early today?
F. - the newest productivity method. chain and battery.""",
395179: """-Let's watch some movie?
-Sure. Get into the movies folder and pick whatever you want yourself.
-So. The cartoons folder. 108GB... don't even want to go in there.
-And rightly not. There are cartoons in there too.""",
395181: """111:come to my place!
222:can't.... grounded(((
111:what's that about?
222:yeah mom decided to "raise" me! the class teacher snitched to her that I stopped studying! she started asking about my homework, and for tomorrow I only have an English text retelling! she doesn't know shit about it, but she made me retell it! well I started telling her with a straight face Nirvana's Smells Like Teen Spirit)))
111:and what did you get punished for? O_o
222:got caught when dad walked in and started singing along(((""",
395182: """The weather in Samara changed from "Pizdets, we're buried" to "Pizdets, we're flooded\"""",
395183: """Uhryab: In Scotland on February 29th by tradition a woman can propose marriage to a man, and if he refuses - he has to pay a fine.
Ksyusha: damn, should've said earlier...)))
Ksyusha: that's it, decided, in four years I'm going to Scotland..
Uhryab: you're ready to chase after every skirt !!??""",
395184: """mwizard: fuck, I've sunk so low (
mwizard: in the official docs I wrote "If the login-password pair is not found in the config.nodes array by the 'login' and 'password' keys, then send them to fuckoffland"
cleg: depends who the docs are for
cleg: on one of our corporate sites clicking the "forgot password" link opened a page with the text "well you're a moron\"""",
395185: """Dаная: but officially I'm like a programmer now
 trite: what do you program in?
 Dаная: I walk around and answer questions 
"why won't my floppy come out - becaaause you stuck it into the CD drive"
"why doesn't my mouse work - probably because the system unit isn't turned on"
and so on
 Dаная: I don't even know what to call this language
 Dаная: the language of reason or something""",
395186: """- Imagine! Just now on National Geographic they showed from start to finish how a cheetah tracked down, caught and devoured a gazelle!
- With passengers?..""",
395187: """After Renault bought part of AvtoVAZ's shares the new car will be called "avno\".""",
395188: """In the office on the microwave as a mat lay two draft sheets with a xerox of a complex drawing. Wrote in the corner of one of them "Find 3 differences". Now work has stopped.""",
395190: """DeepNightBird: The T1000s are hiding in thermometers waiting for hour X!""",
395191: """xxx: You're the embodiment of a phoenix bird, no matter how many times you're banned you still come back)
yyy: the embodiment of what? 
xxx: Fuck, the youth. Like Kenny, basically.""",
395192: """from a forum about iPhones...

You can suggest another method. Since we have 220, and there are two holes in the socket, a simple mathematical calculation shows that there's exactly 110 in one hole. You can take power from just one hole of the socket.""",
395193: """Her: I'm not stupid, right?
Him: the truth is that you have a slightly inflated self-esteem...""",
395194: """<\\\\> loa
<\\\\> I got expelled from the instituto!!
<\\\\> finally!!!
<loa> you're the first who'll be able to clarify something
<\\\\> em?
<loa> is there wifi in the construction battalion...""",
395195: """<Imago>: just now climbed into someone's shared folder and deleted a virus from there)
 <Imago>: feeling like Robin Hood)
 <СНЕЖЕТЬ - 301>: give it to the poor""",
395196: """Lily: I'm burning...
Lily: I've got fire in the microwave...
Lily: a decent one...
Lily: there's flame.... aaaaaaaaaaaaaaaaaa..........
Lily: what do I put it out with?
Lily: it's inside......
Igor: did you think of unplugging it? :))
Lily: no....
Lily: but then it won't finish cooking..""",
395197: """her: Fine, I'll tell you! I'm 18! did anything change?
him: An article of the Criminal Code of the RF )))""",
}

def main():
    src = open('chunks/chunk_13.jsonl', encoding='utf-8').read().splitlines()
    batch = [json.loads(l) for l in src[900:1000]]
    assert len(batch) == 100, len(batch)
    ids = [r['id'] for r in batch]
    assert set(ids) == set(EN), (
        'missing:', sorted(set(ids) - set(EN)),
        'extra:', sorted(set(EN) - set(ids)),
    )
    out = open('out/chunk_13.jsonl', 'a', encoding='utf-8')
    n = 0
    for r in batch:
        en = EN[r['id']]
        assert en, r['id']
        out.write(json.dumps(
            {'id': r['id'], 'ru': r['text'], 'en': en},
            ensure_ascii=False) + '\n')
        n += 1
    out.close()
    print('appended', n)

if __name__ == '__main__':
    main()
